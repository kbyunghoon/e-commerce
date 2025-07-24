package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.OrderDto.OrderCreateDto
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import java.time.LocalDateTime

class OrderServiceTest : BehaviorSpec({
    val orderRepository: OrderRepository = mockk()
    val orderItemRepository: OrderItemRepository = mockk()
    val orderService = OrderService(orderRepository, orderItemRepository)

    afterContainer {
        clearAllMocks()
    }

    Given("주문 생성(createOrder) 시나리오") {
        val userId = 1L
        val orderItems = listOf(OrderItem(productId = 1L, quantity = 10000, pricePerItem = 1))
        val originalAmount = 10000
        val discountAmount = 0
        val finalAmount = 10000
        val couponId = null
        val now = LocalDateTime.now()

        When("유효한 주문 생성 DTO로 주문을 생성하면") {
            val orderCreateDto = OrderCreateDto(
                userId = userId,
                items = orderItems,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                couponId = couponId
            )
            val createdOrder = Order(
                id = 1L,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = couponId,
                orderedAt = now
            )

            every { orderRepository.save(any()) } returns createdOrder
            every { orderItemRepository.saveAll(any()) } returns orderItems

            val result = orderService.createOrder(orderCreateDto)

            Then("주문이 성공적으로 생성되고, 생성된 주문 정보가 반환된다") {
                result.userId shouldBe userId
                result.finalAmount shouldBe finalAmount
                result.status shouldBe OrderStatus.PENDING
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }
    }

    Given("주문 완료(completeOrder) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val orderItems = listOf(OrderItem(orderId = orderId, productId = 1L, quantity = 10000, pricePerItem = 1))
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
                orderedAt = now
            )
            val completedOrder = pendingOrder.copy(status = OrderStatus.COMPLETED)

            val completedOrderItems = orderItems.map { it.copy(status = OrderStatus.COMPLETED) }

            every { orderItemRepository.saveAll(any()) } returns completedOrderItems

            every { orderRepository.findById(orderId) } returns pendingOrder
            every { orderRepository.save(any()) } returns completedOrder
            every { orderItemRepository.findByOrderId(any()) } returns orderItems
            every { orderItemRepository.saveAll(any()) } returns completedOrderItems

            val result = orderService.completePayment(orderId)

            Then("주문 상태가 COMPLETED로 변경되고, 업데이트된 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.status shouldBe OrderStatus.COMPLETED
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("존재하지 않는 주문 ID로 주문 완료를 요청하면") {
            every { orderRepository.findById(orderId) } returns null

            val exception = shouldThrow<BusinessException> {
                orderService.completePayment(orderId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }

        When("이미 완료된 주문을 다시 완료 요청하면") {
            val completedOrder = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.COMPLETED,
                userCouponId = couponId,
                orderedAt = now
            )
            every { orderRepository.findById(orderId) } returns completedOrder

            val exception = shouldThrow<BusinessException> {
                orderService.completePayment(orderId)
            }

            Then("ORDER_ALREADY_PROCESSED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }
    }

    Given("주문 취소(cancelOrder) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val orderItems = listOf(OrderItem(orderId = orderId, productId = 1L, quantity = 10000, pricePerItem = 1))
        val originalAmount = 10000
        val discountAmount = 0
        val finalAmount = 10000
        val couponId = null
        val now = LocalDateTime.now()

        When("존재하는 주문 ID로 주문 취소를 요청하면") {
            val pendingOrder = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = couponId,
                orderedAt = now
            )
            val cancelledOrder = pendingOrder.copy(status = OrderStatus.CANCELLED)

            val cancelledOrderItems = orderItems.map { it.copy(status = OrderStatus.CANCELLED) }

            every { orderRepository.findById(orderId) } returns pendingOrder
            every { orderRepository.save(any()) } returns cancelledOrder
            every { orderItemRepository.findByOrderId(any()) } returns orderItems
            every { orderItemRepository.saveAll(any()) } returns cancelledOrderItems

            val result = orderService.cancelOrder(orderId)

            Then("주문 상태가 CANCELLED로 변경되고, 업데이트된 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.status shouldBe OrderStatus.CANCELLED
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("존재하지 않는 주문 ID로 주문 취소를 요청하면") {
            every { orderRepository.findById(orderId) } returns null

            val exception = shouldThrow<BusinessException> {
                orderService.cancelOrder(orderId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }

        When("이미 취소된 주문을 다시 취소 요청하면") {
            val cancelledOrder = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.CANCELLED,
                userCouponId = couponId,
                orderedAt = now
            )
            every { orderRepository.findById(orderId) } returns cancelledOrder

            val exception = shouldThrow<BusinessException> {
                orderService.cancelOrder(orderId)
            }

            Then("ORDER_ALREADY_CANCELLED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_ALREADY_CANCELLED
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }
    }

    Given("주문 조회(getOrder) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val orderItems = listOf(OrderItem(orderId = orderId, productId = 1L, quantity = 10000, pricePerItem = 1))
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
                orderedAt = now
            )
            every { orderRepository.findById(orderId) } returns order
            every { orderItemRepository.findByOrderId(orderId) } returns orderItems

            val result = orderService.getOrder(orderId)

            Then("해당 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.finalAmount shouldBe finalAmount
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
            }
        }

        When("존재하지 않는 주문 ID로 조회를 요청하면") {
            every { orderRepository.findById(orderId) } returns null

            val exception = shouldThrow<BusinessException> {
                orderService.getOrder(orderId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findById(orderId) }
            }
        }
    }

    Given("결제 완료(completePayment) 시나리오") {
        val orderId = 1L
        val userId = 1L
        val orderItems = listOf(OrderItem(orderId = orderId, productId = 1L, quantity = 10000, pricePerItem = 1))
        val originalAmount = 10000
        val discountAmount = 0
        val finalAmount = 10000
        val couponId = null
        val now = LocalDateTime.now()

        When("존재하는 주문 ID로 결제 완료를 요청하면") {
            val pendingOrder = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = couponId,
                orderedAt = now
            )
            val completedOrder = pendingOrder.copy(status = OrderStatus.COMPLETED)
            val completedOrderItems = orderItems.map { it.copy(status = OrderStatus.COMPLETED) }

            every { orderRepository.findById(orderId) } returns pendingOrder
            every { orderRepository.save(any()) } returns completedOrder
            every { orderItemRepository.findByOrderId(any()) } returns orderItems
            every { orderItemRepository.saveAll(any()) } returns completedOrderItems

            val result = orderService.completePayment(orderId)

            Then("주문 상태가 COMPLETED로 변경되고, 업데이트된 주문 정보가 반환된다") {
                result.id shouldBe orderId
                result.status shouldBe OrderStatus.COMPLETED
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 1) { orderRepository.save(any()) }
                verify(exactly = 1) { orderItemRepository.findByOrderId(orderId) }
                verify(exactly = 1) { orderItemRepository.saveAll(any()) }
            }
        }

        When("존재하지 않는 주문 ID로 결제 완료를 요청하면") {
            every { orderRepository.findById(orderId) } returns null

            val exception = shouldThrow<BusinessException> {
                orderService.completePayment(orderId)
            }

            Then("ORDER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_NOT_FOUND
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }

        When("이미 완료된 주문을 다시 결제 완료 요청하면") {
            val completedOrder = Order(
                id = orderId,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.COMPLETED,
                userCouponId = couponId,
                orderedAt = now
            )
            every { orderRepository.findById(orderId) } returns completedOrder

            val exception = shouldThrow<BusinessException> {
                orderService.completePayment(orderId)
            }

            Then("ORDER_ALREADY_PROCESSED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
                verify(exactly = 1) { orderRepository.findById(orderId) }
                verify(exactly = 0) { orderRepository.save(any()) }
            }
        }
    }
})
