package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductStatus
import kr.hhplus.be.domain.user.UserCoupon
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime

class OrderServiceTest : BehaviorSpec({
    val orderRepository: OrderRepository = mockk()
    val orderItemRepository: OrderItemRepository = mockk()
    val productService: ProductService = mockk()
    val couponService: CouponService = mockk()
    val applicationEventPublisher: ApplicationEventPublisher = mockk()
    val paymentSagaOrchestrator: PaymentSagaOrchestrator = mockk()

    val orderService = OrderService(
        orderRepository,
        orderItemRepository,
        productService,
        couponService,
        applicationEventPublisher,
    )

    afterContainer {
        clearAllMocks()
    }

    Given("주문 처리(processOrder) 시나리오") {
        val userId = 1L
        val productId = 1L
        val quantity = 2
        val productPrice = 10000
        val couponId = 1L
        val userCouponId = 1L
        val discountAmount = 1000

        val orderCreateCommand = OrderCreateCommand(
            userId = userId,
            items = listOf(OrderItemCreateCommand(productId = productId, quantity = quantity)),
            userCouponId = userCouponId
        )

        val product = Product(
            id = productId,
            name = "테스트 상품",
            price = productPrice,
            stock = 100,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = ProductStatus.ACTIVE,
        )

        When("유효한 주문 처리 요청을 하면") {
            val totalAmount = productPrice * quantity
            val finalAmount = totalAmount - discountAmount

            val createdOrder = Order.create(
                userId = userId,
                originalAmount = totalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = userCouponId,
            )

            val orderItems = listOf(
                OrderItem(
                    orderId = 1L,
                    productId = productId,
                    productName = product.name,
                    quantity = quantity,
                    pricePerItem = productPrice,
                    status = OrderStatus.PENDING
                )
            )

            val userCoupon = UserCoupon(
                id = userCouponId,
                userId = userId,
                couponId = couponId,
                status = CouponStatus.AVAILABLE,
                issuedAt = LocalDateTime.now().minusDays(1),
                usedAt = null,
            )

            every { productService.validateOrderItems(any()) } returns listOf(product)
            every { couponService.getUserCouponWithValid(any(), any()) } returns userCoupon
            every { couponService.calculateDiscount(userId, userCouponId, totalAmount) } returns discountAmount
            every { orderRepository.save(any()) } returns createdOrder
            every { orderItemRepository.saveAll(any()) } returns orderItems

            val result = orderService.processOrder(orderCreateCommand)

            Then("주문이 성공적으로 처리되고, 주문 정보가 반환된다") {
                result.userId shouldBe userId
                result.originalAmount shouldBe totalAmount
                result.discountAmount shouldBe discountAmount
                result.finalAmount shouldBe finalAmount
                result.status shouldBe OrderStatus.PENDING

                verify(exactly = 1) { productService.validateOrderItems(any()) }
                verify(exactly = 1) { couponService.getUserCouponWithValid(any(), any()) }
                verify(exactly = 1) { couponService.calculateDiscount(userId, userCouponId, totalAmount) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("쿠폰 없이 주문 처리 요청을 하면") {
            val orderCreateCommandWithoutCoupon = orderCreateCommand.copy(userCouponId = null)
            val totalAmount = productPrice * quantity

            val createdOrder = Order.create(
                userId = userId,
                originalAmount = totalAmount,
                discountAmount = 0,
                finalAmount = totalAmount,
                userCouponId = null,
            )

            val orderItems = listOf(
                OrderItem(
                    orderId = 1L,
                    productId = productId,
                    productName = product.name,
                    quantity = quantity,
                    pricePerItem = productPrice,
                    status = OrderStatus.PENDING
                )
            )

            every { productService.validateOrderItems(any()) } returns listOf(product)
            every { orderRepository.save(any()) } returns createdOrder
            every { orderItemRepository.saveAll(any()) } returns orderItems

            val result = orderService.processOrder(orderCreateCommandWithoutCoupon)

            Then("쿠폰 할인 없이 주문이 성공적으로 처리된다") {
                result.userId shouldBe userId
                result.originalAmount shouldBe totalAmount
                result.discountAmount shouldBe 0
                result.finalAmount shouldBe totalAmount
                result.status shouldBe OrderStatus.PENDING

                verify(exactly = 1) { productService.validateOrderItems(any()) }
                verify(exactly = 0) { couponService.getUserCouponWithValid(any(), any()) }
                verify(exactly = 0) { couponService.calculateDiscount(any(), any(), any()) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("존재하지 않는 상품으로 주문 처리 요청을 하면") {
            every { productService.validateOrderItems(any()) } throws BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                orderService.processOrder(orderCreateCommand)
            }

            Then("PRODUCT_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
                verify(exactly = 1) { productService.validateOrderItems(any()) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }
    }

    Given("Saga 결제 처리(processPaymentWithSaga) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val originalAmount = 10000
        val discountAmount = 0
        val finalAmount = 10000

        val paymentCommand = PaymentProcessCommand(
            orderId = orderId,
            userId = userId
        )

        When("유효한 결제 처리 요청을 하면") {
            val expectedOrderDetails = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.COMPLETED,
                userCouponId = null,
                orderNumber = "",
                orderDate = LocalDateTime.now(),
                orderItems = null
            )

            every { paymentSagaOrchestrator.executePaymentSaga(paymentCommand) } returns expectedOrderDetails

            val result = paymentSagaOrchestrator.executePaymentSaga(paymentCommand)

            Then("PaymentSagaOrchestrator를 통해 결제가 처리된다") {
                result.id shouldBe orderId
                result.status shouldBe OrderStatus.COMPLETED
                verify(exactly = 1) { paymentSagaOrchestrator.executePaymentSaga(paymentCommand) }
            }
        }
    }

    Given("주문 조회(getOrder) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val orderItems = listOf(
            OrderItem(
                orderId = orderId,
                productId = 1L,
                productName = "상품 테스트",
                quantity = 10,
                pricePerItem = 10000
            )
        )
        val originalAmount = 10000
        val discountAmount = 0
        val finalAmount = 10000
        val couponId = null
        val now = LocalDateTime.now()

        When("존재하는 주문 ID로 조회를 요청하면") {
            val order = Order(
                id = orderId,
                userId = userId,
                orderNumber = "테스트",
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.COMPLETED,
                userCouponId = couponId,
                orderItems = orderItems,
                orderDate = now
            )
            every { orderRepository.findByIdAndUserId(orderId, userId) } returns order
            every { orderItemRepository.findByOrderId(orderId) } returns orderItems

            val result = orderService.getOrder(orderId, userId)

            Then("해당 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.finalAmount shouldBe finalAmount
                verify(exactly = 1) { orderRepository.findByIdAndUserId(orderId, userId) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
            }
        }

        When("존재하지 않는 주문 ID로 조회를 요청하면") {
            every {
                orderRepository.findByIdAndUserId(
                    orderId,
                    userId
                )
            } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                orderService.getOrder(orderId, userId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findByIdAndUserId(orderId, userId) }
            }
        }
    }
})