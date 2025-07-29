package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.coupon.CouponDto
import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import kr.hhplus.be.domain.user.UserCoupon
import java.time.LocalDateTime

class OrderServiceTest : BehaviorSpec({
    val orderRepository: OrderRepository = mockk()
    val orderItemRepository: OrderItemRepository = mockk()
    val productService: ProductService = mockk()
    val balanceService: BalanceService = mockk()
    val couponService: CouponService = mockk()

    val orderService = OrderService(
        orderRepository,
        orderItemRepository,
        productService,
        balanceService,
        couponService
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

        val productInfo = ProductDto.ProductInfo(
            id = productId,
            name = "테스트 상품",
            price = productPrice,
            stock = 100,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )

        When("유효한 주문 처리 요청을 하면") {
            val totalAmount = productPrice * quantity
            val finalAmount = totalAmount - discountAmount

            val createdOrder = Order(
                id = 1L,
                userId = userId,
                originalAmount = totalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = userCouponId,
                orderDate = LocalDateTime.now(),
            )

            val orderItems = listOf(
                OrderItem(
                    orderId = 1L,
                    productId = productId,
                    productName = productInfo.name,
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

            val coupon = Coupon(
                id = couponId,
                name = "테스트 쿠폰",
                code = "test-coupon",
                discountType = DiscountType.FIXED,
                discountValue = 1000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = 100,
                issuedQuantity = 1,
            )

            val validatedUserCoupon = CouponDto.ValidatedUserCoupon(
                userCoupon = userCoupon,
                coupon = coupon,
            )

            every { productService.validateOrderItems(any()) } returns listOf(productInfo)
            every { couponService.findAndValidateUserCoupon(any(), any()) } returns validatedUserCoupon
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
                verify(exactly = 1) { couponService.findAndValidateUserCoupon(any(), any()) }
                verify(exactly = 1) { couponService.calculateDiscount(userId, userCouponId, totalAmount) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("쿠폰 없이 주문 처리 요청을 하면") {
            val orderCreateCommandWithoutCoupon = orderCreateCommand.copy(userCouponId = null)
            val totalAmount = productPrice * quantity

            val createdOrder = Order(
                id = 1L,
                userId = userId,
                originalAmount = totalAmount,
                discountAmount = 0,
                finalAmount = totalAmount,
                status = OrderStatus.PENDING,
                userCouponId = null,
                orderDate = LocalDateTime.now()
            )

            val orderItems = listOf(
                OrderItem(
                    orderId = 1L,
                    productId = productId,
                    productName = productInfo.name,
                    quantity = quantity,
                    pricePerItem = productPrice,
                    status = OrderStatus.PENDING
                )
            )

            every { productService.validateOrderItems(any()) } returns listOf(productInfo)
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
                verify(exactly = 0) { couponService.findAndValidateUserCoupon(any(), any()) }
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

    Given("주문 완료(completePayment) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val orderItems =
            listOf(
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

        When("존재하는 주문 ID로 주문 완료를 요청하면") {
            val pendingOrder = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = couponId,
                orderDate = now
            )
            val completedOrder = pendingOrder.copy(status = OrderStatus.COMPLETED)
            val completedOrderItems = orderItems.map { it.copy(status = OrderStatus.COMPLETED) }

            every { orderRepository.findByIdOrThrow(orderId) } returns pendingOrder
            every { orderRepository.save(any()) } returns completedOrder
            every { orderItemRepository.findByOrderId(any()) } returns orderItems
            every { orderItemRepository.saveAll(any()) } returns completedOrderItems

            val result = orderService.completePayment(orderId)

            Then("주문 상태가 COMPLETED로 변경되고, 업데이트된 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.status shouldBe OrderStatus.COMPLETED
                verify(exactly = 1) { orderRepository.findByIdOrThrow(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("존재하지 않는 주문 ID로 주문 완료를 요청하면") {
            every { orderRepository.findByIdOrThrow(orderId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                orderService.completePayment(orderId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findByIdOrThrow(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
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
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.COMPLETED,
                userCouponId = couponId,
                orderDate = now
            )
            every { orderRepository.findByIdOrThrow(orderId) } returns order
            every { orderItemRepository.findByOrderId(orderId) } returns orderItems

            val result = orderService.getOrder(orderId)

            Then("해당 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.finalAmount shouldBe finalAmount
                verify(exactly = 1) { orderRepository.findByIdOrThrow(orderId) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
            }
        }

        When("존재하지 않는 주문 ID로 조회를 요청하면") {
            every { orderRepository.findByIdOrThrow(orderId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                orderService.getOrder(orderId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findByIdOrThrow(orderId) }
            }
        }
    }
})