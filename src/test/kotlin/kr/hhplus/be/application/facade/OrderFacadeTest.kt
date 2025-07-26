package kr.hhplus.be.application.facade

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.order.OrderDto.OrderInfo
import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

class OrderFacadeTest : BehaviorSpec({
    val orderService: OrderService = mockk()
    val productService: ProductService = mockk()
    val balanceService: BalanceService = mockk()
    val couponService: CouponService = mockk()
    val orderFacade = OrderFacade(orderService, productService, balanceService, couponService)

    afterContainer {
        clearAllMocks()
    }

    Given("주문 생성(processOrder) 시나리오") {
        val userId = 1L
        val productId = 1L
        val orderId = 1L
        val orderItems = listOf(OrderItemCreateCommand(productId, 2))
        val productInfo = ProductDto.ProductInfo(
            id = productId,
            name = "Test Product",
            price = 10000,
            stock = 10,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        val totalAmount = 20000

        When("쿠폰 없이 주문을 생성하면") {
            val request = OrderCreateCommand(userId = userId, items = orderItems, couponId = null)
            val createdOrderInfo = OrderInfo(
                id = orderId,
                userId = userId,
                orderItems = emptyList(),
                originalAmount = totalAmount,
                discountAmount = 0,
                finalAmount = totalAmount,
                status = OrderStatus.PENDING,
                orderedAt = LocalDateTime.now()
            )

            every { productService.validateOrderItems(request.items) } returns listOf(productInfo)
            every { couponService.calculateDiscount(any(), any(), any()) } returns 0
            every { orderService.createOrder(any()) } returns createdOrderInfo

            val response = orderFacade.processOrder(request)

            Then("할인 없이 주문이 생성되고, 생성된 주문 정보를 반환한다") {
                response.id shouldBe orderId
                response.finalAmount shouldBe totalAmount
                response.status shouldBe OrderStatus.PENDING

                verify { productService.validateOrderItems(request.items) }
                verify(exactly = 0) { couponService.findAndValidateUserCoupon(any(), any()) }
                verify { orderService.createOrder(any()) }
            }
        }

        When("유효한 쿠폰으로 주문을 생성하면") {
            val couponId = 1L
            val discountAmount = 1000
            val finalAmount = totalAmount - discountAmount
            val request = OrderCreateCommand(userId = userId, items = orderItems, couponId = couponId)
            val createdOrderInfo = OrderInfo(
                id = orderId,
                userId = userId,
                orderItems = emptyList(),
                originalAmount = totalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                orderedAt = LocalDateTime.now()
            )

            every { productService.validateOrderItems(request.items) } returns listOf(productInfo)
            every { couponService.findAndValidateUserCoupon(userId, couponId) } returns mockk()
            every { couponService.calculateDiscount(userId, couponId, totalAmount) } returns discountAmount
            every { orderService.createOrder(any()) } returns createdOrderInfo

            val response = orderFacade.processOrder(request)

            Then("할인이 적용된 주문이 생성되고, 생성된 주문 정보를 반환한다") {
                response.id shouldBe orderId
                response.finalAmount shouldBe finalAmount
                response.discountAmount shouldBe discountAmount

                verify { productService.validateOrderItems(request.items) }
                verify { couponService.findAndValidateUserCoupon(userId, couponId) }
                verify { couponService.calculateDiscount(userId, couponId, totalAmount) }
                verify { orderService.createOrder(any()) }
            }
        }
    }

    Given("결제 처리(processPayment) 시나리오") {
        val userId = 1L
        val orderId = 1L
        val finalAmount = 19000
        val couponId = 1L
        val orderItems =
            listOf(
                OrderDto.OrderItemInfo(productId = 1L, quantity = 1, price = 10000),
                OrderDto.OrderItemInfo(productId = 2L, quantity = 1, price = 9000)
            )

        val pendingOrder = OrderInfo(
            id = orderId,
            userId = userId,
            originalAmount = 19000,
            discountAmount = 0,
            finalAmount = finalAmount,
            status = OrderStatus.PENDING,
            userCouponId = couponId,
            orderItems = orderItems,
            orderedAt = LocalDateTime.now()
        )

        val completedOrderInfo = OrderInfo(
            id = orderId,
            userId = userId,
            orderItems = emptyList(),
            originalAmount = 19000,
            discountAmount = 0,
            finalAmount = finalAmount,
            status = OrderStatus.COMPLETED,
            orderedAt = LocalDateTime.now()
        )

        When("유효한 결제 요청을 처리하면") {
            val request = PaymentProcessCommand(userId = userId, orderId = orderId)

            every { orderService.getOrderForPayment(orderId = orderId, userId = userId) } returns pendingOrder
            every { balanceService.use(any()) } returns mockk()
            every { productService.deductStock(any(), any()) } returns Unit
            every { couponService.use(userId, couponId) } returns mockk()
            every { orderService.completePayment(orderId) } returns completedOrderInfo

            val response = orderFacade.processPayment(request)

            Then("결제 관련 작업(잔액 차감, 재고 차감, 쿠폰 사용)을 수행하고, 주문 상태를 COMPLETED로 변경한다") {
                response.status shouldBe OrderStatus.COMPLETED
                response.id shouldBe orderId

                verify { orderService.getOrderForPayment(orderId = orderId, userId = userId) }
                verify { balanceService.use(any()) }
                verify { productService.deductStock(1L, 1) }
                verify { productService.deductStock(2L, 1) }
                verify { couponService.use(userId, couponId) }
                verify { orderService.completePayment(orderId) }
            }
        }

        When("결제 처리 중 첫 번째 상품의 재고 차감에 실패하면") {
            val request = PaymentProcessCommand(userId = userId, orderId = orderId)

            every { orderService.getOrderForPayment(orderId = orderId, userId = userId) } returns pendingOrder
            every { balanceService.use(any()) } returns mockk()
            every {
                productService.deductStock(
                    orderItems[0].productId,
                    orderItems[0].quantity
                )
            } throws BusinessException(ErrorCode.INSUFFICIENT_STOCK)

            every { balanceService.refund(any(), any()) } returns mockk()
            every { productService.restoreStock(any(), any()) } returns Unit
            every { couponService.restore(any(), any()) } returns mockk()

            val exception = shouldThrow<BusinessException> {
                orderFacade.processPayment(request)
            }

            Then("잔액이 복구되고, 재고 및 쿠폰은 복구되지 않으며 예외가 다시 발생한다") {
                exception.errorCode shouldBe ErrorCode.INSUFFICIENT_STOCK

                verify { orderService.getOrderForPayment(orderId = orderId, userId = userId) }
                verify { balanceService.use(any()) }
                verify { productService.deductStock(orderItems[0].productId, orderItems[0].quantity) }

                verify { balanceService.refund(userId, finalAmount) }
                verify(exactly = 0) { productService.restoreStock(any(), any()) }
                verify(exactly = 0) { couponService.restore(any(), any()) }
            }
        }

        When("결제 처리 중 쿠폰 사용에 실패하면") {
            val request = PaymentProcessCommand(userId = userId, orderId = orderId)

            every { orderService.getOrderForPayment(orderId = orderId, userId = userId) } returns pendingOrder
            every { balanceService.use(any()) } returns mockk()
            every { productService.deductStock(any(), any()) } returns Unit
            every { couponService.use(userId, couponId) } throws BusinessException(ErrorCode.COUPON_NOT_AVAILABLE)

            every { balanceService.refund(any(), any()) } returns mockk()
            every { productService.restoreStock(any(), any()) } returns Unit
            every { couponService.restore(any(), any()) } returns mockk()

            val exception = shouldThrow<BusinessException> {
                orderFacade.processPayment(request)
            }

            Then("잔액과 모든 상품 재고가 복구되고 예외가 다시 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE

                verify { orderService.getOrderForPayment(orderId = orderId, userId = userId) }
                verify { balanceService.use(any()) }
                verify { productService.deductStock(orderItems[0].productId, orderItems[0].quantity) }
                verify { productService.deductStock(orderItems[1].productId, orderItems[1].quantity) }
                verify { couponService.use(userId, couponId) }

                verify { balanceService.refund(userId, finalAmount) }
                verify { productService.restoreStock(orderItems[0].productId, orderItems[0].quantity) }
                verify { productService.restoreStock(orderItems[1].productId, orderItems[1].quantity) }
                verify(exactly = 0) { couponService.restore(any(), any()) }
            }
        }

        When("주문 소유자가 아닌 다른 사용자가 결제를 시도하면") {
            val anotherUserId = 2L
            val request = PaymentProcessCommand(userId = anotherUserId, orderId = orderId)

            every { orderService.getOrderForPayment(orderId = orderId, userId = anotherUserId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                orderFacade.processPayment(request)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                
                verify { orderService.getOrderForPayment(orderId = orderId, userId = anotherUserId) }
            }
        }

        When("이미 처리된 주문에 대해 결제를 시도하면") {
            val request = PaymentProcessCommand(userId = userId, orderId = orderId)

            every { orderService.getOrderForPayment(orderId = orderId, userId = userId) } throws BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)

            val exception = shouldThrow<BusinessException> {
                orderFacade.processPayment(request)
            }

            Then("ORDER_ALREADY_PROCESSED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
                
                verify { orderService.getOrderForPayment(orderId = orderId, userId = userId) }
            }
        }
    }

    Given("주문 조회(getOrder) 시나리오") {
        val userId = 1L
        val orderId = 1L
        val orderInfo = OrderInfo(
            id = orderId,
            userId = userId,
            orderItems = emptyList(),
            originalAmount = 10000,
            discountAmount = 0,
            finalAmount = 10000,
            status = OrderStatus.COMPLETED,
            orderedAt = LocalDateTime.now()
        )

        When("존재하는 주문 ID로 조회를 요청하면") {
            every { orderService.getOrder(orderId) } returns orderInfo

            val response = orderFacade.getOrder(userId, orderId)

            Then("해당 주문 정보를 반환한다") {
                response.id shouldBe orderId
                response.finalAmount shouldBe 10000

                verify { orderService.getOrder(orderId) }
            }
        }
    }
})
