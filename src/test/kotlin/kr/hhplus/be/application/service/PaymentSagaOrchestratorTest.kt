package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.coupon.CouponDto
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import kr.hhplus.be.domain.order.saga.PaymentSaga
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import kr.hhplus.be.domain.order.saga.PaymentSagaStep
import kr.hhplus.be.domain.order.saga.SagaStatus
import kr.hhplus.be.domain.order.saga.events.*
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class PaymentSagaOrchestratorTest : BehaviorSpec({
    val sagaRepository: PaymentSagaRepository = mockk()
    val orderService: OrderService = mockk()
    val applicationEventPublisher: ApplicationEventPublisher = mockk(relaxed = true)

    val paymentSagaOrchestrator = PaymentSagaOrchestrator(
        sagaRepository,
        orderService,
        applicationEventPublisher,
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

        val order = Order(
            id = orderId,
            userId = userId,
            orderNumber = "ORDER-001",
            originalAmount = 12000,
            discountAmount = 2000,
            finalAmount = finalAmount,
            status = OrderStatus.PENDING,
            userCouponId = userCouponId,
            orderDate = LocalDateTime.now()
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

        val userCouponInfo = CouponDto.UserCouponInfo(
            id = userCouponId,
            userId = userId,
            couponId = 1L,
            couponName = "테스트 쿠폰",
            discountType = DiscountType.FIXED,
            discountValue = 2000,
            status = CouponStatus.AVAILABLE,
            expiresAt = LocalDateTime.now().plusDays(30),
            issuedAt = LocalDateTime.now(),
            usedAt = null
        )

        When("정상적인 결제 사가 실행 요청을 하면") {
            val orderDto = kr.hhplus.be.application.order.OrderDto.OrderDetails.from(order, orderItems)
            every { orderService.getOrderForPayment(orderId, userId) } returns orderDto
            every { sagaRepository.save(any()) } returnsArgument 0

            val result = paymentSagaOrchestrator.executePaymentSaga(command)

            Then("Saga가 생성되고 PaymentSagaStartedEvent가 발행된다") {
                result.id shouldBe orderId
                result.userId shouldBe userId
                result.finalAmount shouldBe finalAmount

                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<PaymentSagaStartedEvent>()) }
            }
        }

        When("쿠폰 없는 주문으로 결제 사가를 실행하면") {
            val orderWithoutCoupon = order.copy(userCouponId = null, discountAmount = 0, finalAmount = 12000)
            val orderDtoWithoutCoupon = kr.hhplus.be.application.order.OrderDto.OrderDetails.from(orderWithoutCoupon, orderItems)

            every { orderService.getOrderForPayment(orderId, userId) } returns orderDtoWithoutCoupon
            every { sagaRepository.save(any()) } returnsArgument 0

            val result = paymentSagaOrchestrator.executePaymentSaga(command)

            Then("Saga가 생성되고 이벤트가 발행된다") {
                result.finalAmount shouldBe 12000

                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<PaymentSagaStartedEvent>()) }
            }
        }

        When("존재하지 않는 주문으로 결제 사가를 실행하면") {
            every { orderService.getOrderForPayment(orderId, userId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 0) { sagaRepository.save(any()) }
            }
        }

        When("다른 사용자의 주문으로 결제 사가를 실행하면") {
            every { orderService.getOrderForPayment(orderId, userId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 0) { sagaRepository.save(any()) }
            }
        }

        When("이미 처리된 주문으로 결제 사가를 실행하면") {
            every { orderService.getOrderForPayment(orderId, userId) } throws BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("ORDER_ALREADY_PROCESSED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
                verify(exactly = 0) { sagaRepository.save(any()) }
            }
        }
    }

    Given("이벤트 핸들러 테스트") {
        val sagaId = "test-saga-id"
        val orderId = 1L
        val userId = 1L
        val finalAmount = 10000
        val userCouponId = 1L

        val saga = PaymentSaga(
            sagaId = sagaId,
            orderId = orderId,
            userId = userId,
            finalAmount = finalAmount,
            userCouponId = userCouponId,
            currentStep = PaymentSagaStep.CREATE_ORDER,
            status = SagaStatus.STARTED
        )

        When("BalanceDeductedEvent를 받으면") {
            val event = BalanceDeductedEvent(
                sagaId = sagaId,
                orderId = orderId,
                userId = userId,
                amount = finalAmount
            )

            every { sagaRepository.findBySagaId(sagaId) } returns saga
            every { sagaRepository.save(any()) } returnsArgument 0

            paymentSagaOrchestrator.handleBalanceDeducted(event)

            Then("다음 단계인 StockDeductionRequestedEvent가 발행된다") {
                verify(exactly = 1) { sagaRepository.findBySagaId(sagaId) }
                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<StockDeductionRequestedEvent>()) }
            }
        }

        When("StockDeductedEvent를 받으면") {
            val event = StockDeductedEvent(
                sagaId = sagaId,
                orderId = orderId
            )

            val updatedSaga = saga.markStepCompleted(PaymentSagaStep.DEDUCT_BALANCE)
            every { sagaRepository.findBySagaId(sagaId) } returns updatedSaga
            every { sagaRepository.save(any()) } returnsArgument 0

            paymentSagaOrchestrator.handleStockDeducted(event)

            Then("쿠폰이 있으면 CouponUsageRequestedEvent가 발행된다") {
                verify(exactly = 1) { sagaRepository.findBySagaId(sagaId) }
                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<CouponUsageRequestedEvent>()) }
            }
        }

        When("StockDeductedEvent를 받고 쿠폰이 없으면") {
            val sagaWithoutCoupon = saga.copy(userCouponId = null)
            val event = StockDeductedEvent(
                sagaId = sagaId,
                orderId = orderId
            )

            every { sagaRepository.findBySagaId(sagaId) } returns sagaWithoutCoupon
            every { sagaRepository.save(any()) } returnsArgument 0

            paymentSagaOrchestrator.handleStockDeducted(event)

            Then("바로 OrderCompletionRequestedEvent가 발행된다") {
                verify(exactly = 1) { sagaRepository.findBySagaId(sagaId) }
                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<OrderCompletionRequestedEvent>()) }
            }
        }

        When("CouponUsedEvent를 받으면") {
            val event = CouponUsedEvent(
                sagaId = sagaId,
                orderId = orderId,
                userId = userId,
                couponId = userCouponId
            )

            val updatedSaga = saga.markStepCompleted(PaymentSagaStep.DEDUCT_STOCK)
            every { sagaRepository.findBySagaId(sagaId) } returns updatedSaga
            every { sagaRepository.save(any()) } returnsArgument 0

            paymentSagaOrchestrator.handleCouponUsed(event)

            Then("OrderCompletionRequestedEvent가 발행된다") {
                verify(exactly = 1) { sagaRepository.findBySagaId(sagaId) }
                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<OrderCompletionRequestedEvent>()) }
            }
        }

        When("OrderCompletedEvent를 받으면") {
            val event = OrderCompletedEvent(
                sagaId = sagaId,
                orderId = orderId
            )

            val updatedSaga = saga.markStepCompleted(PaymentSagaStep.USE_COUPON)
            every { sagaRepository.findBySagaId(sagaId) } returns updatedSaga
            every { sagaRepository.save(any()) } returnsArgument 0

            paymentSagaOrchestrator.handleOrderCompleted(event)

            Then("PaymentSagaCompletedEvent가 발행되고 Saga가 완료된다") {
                verify(exactly = 1) { sagaRepository.findBySagaId(sagaId) }
                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<PaymentSagaCompletedEvent>()) }
            }
        }

        When("BalanceDeductionFailedEvent를 받으면") {
            val event = BalanceDeductionFailedEvent(
                sagaId = sagaId,
                orderId = orderId,
                userId = userId,
                amount = finalAmount,
                reason = "Insufficient balance"
            )

            every { sagaRepository.findBySagaId(sagaId) } returns saga
            every { sagaRepository.save(any()) } returnsArgument 0

            paymentSagaOrchestrator.handleBalanceDeductionFailed(event)

            Then("PaymentSagaFailedEvent가 발행되고 보상 트랜잭션이 시작된다") {
                verify(exactly = 1) { sagaRepository.findBySagaId(sagaId) }
                verify(exactly = 1) { sagaRepository.save(any()) }
                verify(atLeast = 1) { applicationEventPublisher.publishEvent(any<PaymentSagaFailedEvent>()) }
            }
        }
    }
})