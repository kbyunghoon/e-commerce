package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.service.saga.SagaCouponService
import kr.hhplus.be.application.service.saga.SagaOrderService
import kr.hhplus.be.application.service.saga.SagaProductService
import kr.hhplus.be.application.service.saga.SagaUserService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import java.time.LocalDateTime

class PaymentSagaOrchestratorTest : BehaviorSpec({
    val sagaRepository: PaymentSagaRepository = mockk()
    val sagaOrderService: SagaOrderService = mockk()
    val sagaUserService: SagaUserService = mockk()
    val sagaProductService: SagaProductService = mockk()
    val sagaCouponService: SagaCouponService = mockk()

    val paymentSagaOrchestrator = PaymentSagaOrchestrator(
        sagaRepository,
        sagaOrderService,
        sagaUserService,
        sagaProductService,
        sagaCouponService,
    )

    afterContainer {
        clearAllMocks()
    }

    Given("결제 사가 처리(executePaymentSaga) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val finalAmount = 10000
        val userCouponId = 1L

        val command = PaymentProcessCommand(
            orderId = orderId,
            userId = userId
        )

        val orderItems = listOf(
            OrderItem(
                orderId = orderId,
                productId = 1L,
                productName = "테스트 상품",
                quantity = 2,
                pricePerItem = 5000,
                status = OrderStatus.PENDING
            )
        )


        val order = Order(
            id = orderId,
            userId = userId,
            orderNumber = "ORDER-001",
            originalAmount = 12000,
            discountAmount = 2000,
            finalAmount = finalAmount,
            status = OrderStatus.PENDING,
            userCouponId = userCouponId,
            orderItems = orderItems,
            orderDate = LocalDateTime.now()
        )

        When("정상적인 결제 사가 실행 요청을 하면") {
            every { sagaOrderService.getOrderForPayment(orderId, userId) } returns order
            every { sagaRepository.save(any()) } returnsArgument 0
            every { sagaUserService.deductBalance(userId, finalAmount) } returns Unit
            every { sagaProductService.deductStock(orderId, userId) } returns Unit
            every { sagaCouponService.useCoupon(userId, userCouponId) } returns Unit
            every { sagaOrderService.completeOrder(orderId) } returns Unit
            every { sagaOrderService.getOrder(orderId, userId) } returns order

            val result = paymentSagaOrchestrator.executePaymentSaga(command)

            Then("Saga가 완료되고 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.userId shouldBe userId
                result.finalAmount shouldBe finalAmount

                verify(exactly = 5) { sagaRepository.save(any()) } // 초기화 + 4단계 완료
                verify(exactly = 1) { sagaUserService.deductBalance(userId, finalAmount) }
                verify(exactly = 1) { sagaProductService.deductStock(orderId, userId) }
                verify(exactly = 1) { sagaCouponService.useCoupon(userId, userCouponId) }
                verify(exactly = 1) { sagaOrderService.completeOrder(orderId) }
            }
        }

        When("쿠폰 없는 주문으로 결제 사가를 실행하면") {
            val orderWithoutCoupon = order.copy(userCouponId = null, discountAmount = 0, finalAmount = 12000)

            every { sagaOrderService.getOrderForPayment(orderId, userId) } returns orderWithoutCoupon
            every { sagaRepository.save(any()) } returnsArgument 0
            every { sagaUserService.deductBalance(userId, 12000) } returns Unit
            every { sagaProductService.deductStock(orderId, userId) } returns Unit
            every { sagaOrderService.completeOrder(orderId) } returns Unit
            every { sagaOrderService.getOrder(orderId, userId) } returns orderWithoutCoupon

            val result = paymentSagaOrchestrator.executePaymentSaga(command)

            Then("Saga가 완료되고 쿠폰 단계는 건너뛴다") {
                result.finalAmount shouldBe 12000

                verify(exactly = 4) { sagaRepository.save(any()) } // 초기화 + 3단계 완료 (쿠폰 제외)
                verify(exactly = 1) { sagaUserService.deductBalance(userId, 12000) }
                verify(exactly = 1) { sagaProductService.deductStock(orderId, userId) }
                verify(exactly = 0) { sagaCouponService.useCoupon(any(), any()) } // 쿠폰 단계 건너뛰기
                verify(exactly = 1) { sagaOrderService.completeOrder(orderId) }
            }
        }

        When("존재하지 않는 주문으로 결제 사가를 실행하면") {
            every {
                sagaOrderService.getOrderForPayment(
                    orderId,
                    userId
                )
            } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 0) { sagaRepository.save(any()) }
            }
        }

        When("재고 부족으로 Saga가 실패하면") {
            every { sagaOrderService.getOrderForPayment(orderId, userId) } returns order
            every { sagaRepository.save(any()) } returnsArgument 0
            every { sagaUserService.deductBalance(userId, finalAmount) } returns Unit
            every {
                sagaProductService.deductStock(
                    orderId,
                    userId
                )
            } throws BusinessException(ErrorCode.INSUFFICIENT_STOCK)
            every { sagaUserService.refundBalance(userId, finalAmount) } returns Unit

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("보상 트랜잭션이 실행되고 PAYMENT_PROCESSING_FAILED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.PAYMENT_PROCESSING_FAILED
                verify(exactly = 4) { sagaRepository.save(any()) }
                verify(exactly = 1) { sagaUserService.deductBalance(userId, finalAmount) }
                verify(exactly = 1) { sagaUserService.refundBalance(userId, finalAmount) }
            }
        }
    }
})