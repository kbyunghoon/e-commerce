package kr.hhplus.be.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode

class OrderTest : FunSpec({

    test("정상 주문을 생성하면 주문이 정상 생성된다") {
        // Given
        val orderItems = listOf(
            OrderItem(1L, 1L, 10000, 2),
            OrderItem(1L, 2L, 15000, 1)
        )

        // When
        val order = Order.create(
            userId = 1L,
            originalAmount = 35000,
            discountAmount = 5000,
            finalAmount = 30000,
            userCouponId = 1L
        )

        // Then
        order.userId shouldBe 1L
        order.originalAmount shouldBe 35000
        order.discountAmount shouldBe 5000
        order.finalAmount shouldBe 30000
        order.status shouldBe OrderStatus.PENDING
        order.userCouponId shouldBe 1L
    }

    test("대기 상태의 주문을 완료하면 상태가 완료로 변경된다") {
        // Given
        val order = Order.create(
            userId = 1L,
            originalAmount = 10000,
            discountAmount = 0,
            finalAmount = 10000,
            userCouponId = null
        )

        // When
        order.completeOrder()

        // Then
        order.status shouldBe OrderStatus.COMPLETED
    }

    test("이미 완료된 주문을 다시 완료하려고 하면 예외가 발생한다") {
        // Given
        val order = Order.create(
            userId = 1L,
            originalAmount = 10000,
            discountAmount = 0,
            finalAmount = 10000,
            userCouponId = null
        ).copy(status = OrderStatus.COMPLETED)

        // When & Then
        shouldThrow<BusinessException> {
            order.completeOrder()
        }.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
    }

    test("주문을 취소하면 상태가 취소로 변경된다") {
        // Given
        val order = Order.create(
            userId = 1L,
            originalAmount = 10000,
            discountAmount = 0,
            finalAmount = 10000,
            userCouponId = null
        )

        // When
        order.cancelOrder()

        // Then
        order.status shouldBe OrderStatus.CANCELLED
    }

    test("이미 취소된 주문을 다시 취소하려고 하면 예외가 발생한다") {
        // Given
        val order = Order.create(
            userId = 1L,
            originalAmount = 10000,
            discountAmount = 0,
            finalAmount = 10000,
            userCouponId = null
        ).copy(status = OrderStatus.CANCELLED)

        // When & Then
        shouldThrow<BusinessException> {
            order.cancelOrder()
        }.errorCode shouldBe ErrorCode.ORDER_ALREADY_CANCELLED
    }
})
