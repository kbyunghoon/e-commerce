package kr.hhplus.be.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

class OrderTest : FunSpec({

    context("Order 생성 테스트") {

        test("정상적인 주문을 생성할 수 있다") {
            // Given
            val userId = 1L
            val originalAmount = 35000
            val discountAmount = 5000
            val finalAmount = 30000
            val userCouponId = 1L

            // When
            val order = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = userCouponId
            )

            // Then
            order.userId shouldBe userId
            order.originalAmount shouldBe originalAmount
            order.discountAmount shouldBe discountAmount
            order.finalAmount shouldBe finalAmount
            order.status shouldBe OrderStatus.PENDING
            order.userCouponId shouldBe userCouponId
            order.expireDate shouldNotBe null
            order.orderDate shouldBe null
        }

        test("쿠폰 없이 주문을 생성할 수 있다") {
            // Given
            val userId = 1L
            val originalAmount = 10000
            val discountAmount = 0
            val finalAmount = 10000

            // When
            val order = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null
            )

            // Then
            order.userId shouldBe userId
            order.originalAmount shouldBe originalAmount
            order.discountAmount shouldBe discountAmount
            order.finalAmount shouldBe finalAmount
            order.status shouldBe OrderStatus.PENDING
            order.userCouponId shouldBe null
        }

        test("사용자 ID가 0 이하이면 예외가 발생한다") {
            // Given
            val invalidUserId = 0L

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = invalidUserId,
                    originalAmount = 10000,
                    discountAmount = 0,
                    finalAmount = 10000,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.INVALID_USER_ID
        }

        test("주문 금액이 최소값보다 작으면 예외가 발생한다") {
            // Given
            val invalidAmount = 0

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = 1L,
                    originalAmount = invalidAmount,
                    discountAmount = 0,
                    finalAmount = invalidAmount,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.INVALID_ORDER_AMOUNT
        }

        test("주문 금액이 최대값보다 크면 예외가 발생한다") {
            // Given
            val invalidAmount = 1_000_001

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = 1L,
                    originalAmount = invalidAmount,
                    discountAmount = 0,
                    finalAmount = invalidAmount,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.INVALID_ORDER_AMOUNT
        }

        test("할인 금액이 음수이면 예외가 발생한다") {
            // Given
            val invalidDiscountAmount = -1000

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = 1L,
                    originalAmount = 10000,
                    discountAmount = invalidDiscountAmount,
                    finalAmount = 11000,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.INVALID_DISCOUNT_AMOUNT
        }

        test("할인 금액이 주문 금액보다 크면 예외가 발생한다") {
            // Given
            val originalAmount = 10000
            val discountAmount = 15000

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = 1L,
                    originalAmount = originalAmount,
                    discountAmount = discountAmount,
                    finalAmount = -5000,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.DISCOUNT_EXCEEDS_ORDER_AMOUNT
        }


        test("금액 계산이 올바르지 않으면 예외가 발생한다") {
            // Given
            val originalAmount = 10000
            val discountAmount = 2000
            val wrongFinalAmount = 9000

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = 1L,
                    originalAmount = originalAmount,
                    discountAmount = discountAmount,
                    finalAmount = wrongFinalAmount,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.INVALID_AMOUNT_CALCULATION
        }

        test("할인율이 100%를 초과하면 예외가 발생한다") {
            // Given - 할인율이 100%를 초과하는 경우는 할인 금액이 주문 금액보다 클 때
            val originalAmount = 10000
            val discountAmount = 10001

            // When & Then
            shouldThrow<BusinessException> {
                Order.create(
                    userId = 1L,
                    originalAmount = originalAmount,
                    discountAmount = discountAmount,
                    finalAmount = -1,
                    userCouponId = null
                )
            }.errorCode shouldBe ErrorCode.DISCOUNT_EXCEEDS_ORDER_AMOUNT
        }
    }

    context("Order 상태 변경 테스트") {

        test("대기 상태의 주문을 완료할 수 있다") {
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
            order.isCompleted() shouldBe true
            order.isPending() shouldBe false
        }

        test("이미 완료된 주문을 다시 완료하려고 하면 예외가 발생한다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )
            order.completeOrder()

            // When & Then
            shouldThrow<BusinessException> {
                order.completeOrder()
            }.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
        }

        test("대기 상태의 주문을 취소할 수 있다") {
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
            order.isCancelled() shouldBe true
            order.isPending() shouldBe false
        }

        test("완료된 주문을 취소할 수 있다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )
            order.completeOrder()

            // When
            order.cancelOrder()

            // Then
            order.status shouldBe OrderStatus.CANCELLED
            order.isCancelled() shouldBe true
            order.isCompleted() shouldBe false
        }

        test("이미 취소된 주문을 다시 취소하려고 하면 예외가 발생한다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )
            order.cancelOrder()

            // When & Then
            shouldThrow<BusinessException> {
                order.cancelOrder()
            }.errorCode shouldBe ErrorCode.ORDER_ALREADY_CANCELLED
        }

        test("취소된 주문을 완료하려고 하면 예외가 발생한다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )
            order.cancelOrder()

            // When & Then
            shouldThrow<BusinessException> {
                order.completeOrder()
            }.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
        }
    }

    context("Order 상태 확인 메서드 테스트") {

        test("새로 생성된 주문은 대기 상태이다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )

            // Then
            order.isPending() shouldBe true
            order.isCompleted() shouldBe false
            order.isCancelled() shouldBe false
            order.canBeCompleted() shouldBe true
            order.canBeCancelled() shouldBe true
        }

        test("완료된 주문의 상태를 올바르게 확인할 수 있다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )
            order.completeOrder()

            // Then
            order.isPending() shouldBe false
            order.isCompleted() shouldBe true
            order.isCancelled() shouldBe false
            order.canBeCompleted() shouldBe false
            order.canBeCancelled() shouldBe true
        }

        test("취소된 주문의 상태를 올바르게 확인할 수 있다") {
            // Given
            val order = Order.create(
                userId = 1L,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                userCouponId = null
            )
            order.cancelOrder()

            // Then
            order.isPending() shouldBe false
            order.isCompleted() shouldBe false
            order.isCancelled() shouldBe true
            order.canBeCompleted() shouldBe false
            order.canBeCancelled() shouldBe false
        }
    }

    context("Order 불변 조건 검증 테스트") {
        test("불변 조건을 위반하는 Order 객체로 상태 변경 시 예외가 발생한다") {
            // Given
            val invalidOrder = Order(
                id = 1L,
                orderNumber = "테스트",
                userId = 1L,
                userCouponId = null,
                originalAmount = 0,
                discountAmount = 0,
                finalAmount = 0,
                status = OrderStatus.PENDING,
                orderDate = LocalDateTime.now(),
                createdAt = LocalDateTime.now()
            )

            // When & Then
            shouldThrow<BusinessException> {
                invalidOrder.completeOrder()
            }.errorCode shouldBe ErrorCode.INVALID_ORDER_AMOUNT
        }
    }
})
