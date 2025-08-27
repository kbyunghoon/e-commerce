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
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import java.time.LocalDateTime

class PaymentSagaOrchestratorTest : BehaviorSpec({
    val sagaRepository: PaymentSagaRepository = mockk()
    val balanceService: BalanceService = mockk()
    val productService: ProductService = mockk()
    val couponService: CouponService = mockk()
    val orderService: OrderService = mockk()

    val paymentSagaOrchestrator = PaymentSagaOrchestrator(
        sagaRepository,
        balanceService,
        productService,
        couponService,
        orderService,
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
            val completedOrderDto = orderDto.copy(status = OrderStatus.COMPLETED)
            every { orderService.getOrderForPayment(orderId, userId) } returns orderDto
            every { orderService.getOrder(orderId, userId) } returns completedOrderDto
            every { orderService.completeOrderForSaga(orderId) } returns Unit
            every { sagaRepository.save(any()) } returnsArgument 0
            every { balanceService.deductBalanceForSaga(userId, finalAmount) } returns Unit
            every { orderService.deductStockForSaga(orderId, userId) } returns Unit
            every { couponService.use(userId, userCouponId) } returns userCouponInfo

            val result = paymentSagaOrchestrator.executePaymentSaga(command)

            Then("모든 사가 단계가 성공적으로 실행되고 완료된 주문이 반환된다") {
                result.id shouldBe orderId
                result.userId shouldBe userId
                result.status shouldBe OrderStatus.COMPLETED
                result.finalAmount shouldBe finalAmount

                verify(exactly = 1) { balanceService.deductBalanceForSaga(userId, finalAmount) }
                verify(exactly = 1) { orderService.deductStockForSaga(orderId, userId) }
                verify(exactly = 1) { couponService.use(userId, userCouponId) }
                verify(atLeast = 5) { sagaRepository.save(any()) }
            }
        }

        When("쿠폰 없는 주문으로 결제 사가를 실행하면") {
            val orderWithoutCoupon = order.copy(userCouponId = null, discountAmount = 0, finalAmount = 12000)
            val orderDtoWithoutCoupon = kr.hhplus.be.application.order.OrderDto.OrderDetails.from(orderWithoutCoupon, orderItems)
            val completedOrderDtoWithoutCoupon = orderDtoWithoutCoupon.copy(status = OrderStatus.COMPLETED)

            every { orderService.getOrderForPayment(orderId, userId) } returns orderDtoWithoutCoupon
            every { orderService.getOrder(orderId, userId) } returns completedOrderDtoWithoutCoupon
            every { orderService.completeOrderForSaga(orderId) } returns Unit
            every { sagaRepository.save(any()) } returnsArgument 0
            every { balanceService.deductBalanceForSaga(userId, 12000) } returns Unit
            every { orderService.deductStockForSaga(orderId, userId) } returns Unit

            val result = paymentSagaOrchestrator.executePaymentSaga(command)

            Then("쿠폰 사용 단계를 제외하고 사가가 성공적으로 실행된다") {
                result.status shouldBe OrderStatus.COMPLETED
                result.finalAmount shouldBe 12000

                verify(exactly = 1) { balanceService.deductBalanceForSaga(userId, 12000) }
                verify(exactly = 1) { orderService.deductStockForSaga(orderId, userId) }
                verify(exactly = 0) { couponService.use(any(), any()) }
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

        When("잔액 부족으로 결제가 실패하면") {
            val orderDto = kr.hhplus.be.application.order.OrderDto.OrderDetails.from(order, orderItems)
            every { orderService.getOrderForPayment(orderId, userId) } returns orderDto
            every { orderService.cancelOrderForSaga(orderId) } returns Unit
            every { sagaRepository.save(any()) } returnsArgument 0
            every {
                balanceService.deductBalanceForSaga(
                    userId,
                    finalAmount
                )
            } throws BusinessException(ErrorCode.INSUFFICIENT_BALANCE)
            every { balanceService.refundBalanceForSaga(userId, finalAmount) } returns Unit

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("INSUFFICIENT_BALANCE 예외가 발생한다 (첫 번째 단계 실패로 보상 불필요)") {
                exception.errorCode shouldBe ErrorCode.INSUFFICIENT_BALANCE
                verify(exactly = 0) { balanceService.refundBalanceForSaga(any(), any()) }
            }
        }

        When("재고 부족으로 결제가 실패하면") {
            val orderDto = kr.hhplus.be.application.order.OrderDto.OrderDetails.from(order, orderItems)
            every { orderService.getOrderForPayment(orderId, userId) } returns orderDto
            every { orderService.cancelOrderForSaga(orderId) } returns Unit
            every { sagaRepository.save(any()) } returnsArgument 0
            every { balanceService.deductBalanceForSaga(userId, finalAmount) } returns Unit
            every {
                orderService.deductStockForSaga(
                    orderId,
                    userId
                )
            } throws BusinessException(ErrorCode.INSUFFICIENT_STOCK)
            every { balanceService.refundBalanceForSaga(userId, finalAmount) } returns Unit
            every { productService.restoreStockForSaga(orderId) } returns Unit

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("INSUFFICIENT_STOCK 예외가 발생하고 잔액만 보상된다") {
                exception.errorCode shouldBe ErrorCode.INSUFFICIENT_STOCK
                verify(exactly = 1) { balanceService.refundBalanceForSaga(userId, finalAmount) }
                verify(exactly = 0) { productService.restoreStockForSaga(orderId) }
            }
        }

        When("쿠폰 사용 중 결제가 실패하면") {
            val orderDto = kr.hhplus.be.application.order.OrderDto.OrderDetails.from(order, orderItems)
            every { orderService.getOrderForPayment(orderId, userId) } returns orderDto
            every { orderService.cancelOrderForSaga(orderId) } returns Unit
            every { sagaRepository.save(any()) } returnsArgument 0
            every { balanceService.deductBalanceForSaga(userId, finalAmount) } returns Unit
            every { orderService.deductStockForSaga(orderId, userId) } returns Unit
            every {
                couponService.use(
                    userId,
                    userCouponId
                )
            } throws BusinessException(ErrorCode.COUPON_NOT_AVAILABLE)
            every { balanceService.refundBalanceForSaga(userId, finalAmount) } returns Unit
            every { productService.restoreStockForSaga(orderId) } returns Unit
            every { couponService.restoreCouponForSaga(userId, userCouponId) } returns Unit

            val exception = shouldThrow<BusinessException> {
                paymentSagaOrchestrator.executePaymentSaga(command)
            }

            Then("COUPON_ALREADY_USED 예외가 발생하고 잔액과 재고가 보상된다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
                verify(exactly = 1) { balanceService.refundBalanceForSaga(userId, finalAmount) }
                verify(exactly = 1) { productService.restoreStockForSaga(orderId) }
                verify(exactly = 0) { couponService.restoreCouponForSaga(any(), any()) }
            }
        }
    }
})