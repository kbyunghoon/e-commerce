package kr.hhplus.be.domain.order

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class PaymentTest : FunSpec({

    context("Payment 생성 테스트") {
        
        test("정상적인 결제 정보를 생성할 수 있다") {
            // Given
            val orderId = 1L
            val orderNumber = "ORDER-20240101-001"
            val userId = 1L
            val finalAmount = 25000
            val status = OrderStatus.COMPLETED
            val orderedAt = LocalDateTime.now()

            // When
            val payment = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            // Then
            payment.orderId shouldBe orderId
            payment.orderNumber shouldBe orderNumber
            payment.userId shouldBe userId
            payment.finalAmount shouldBe finalAmount
            payment.status shouldBe status
            payment.orderedAt shouldBe orderedAt
        }

        test("결제 대기 상태의 결제 정보를 생성할 수 있다") {
            // Given
            val orderId = 2L
            val orderNumber = "ORDER-20240101-002"
            val userId = 2L
            val finalAmount = 15000
            val status = OrderStatus.PENDING
            val orderedAt = LocalDateTime.now()

            // When
            val payment = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            // Then
            payment.orderId shouldBe orderId
            payment.orderNumber shouldBe orderNumber
            payment.userId shouldBe userId
            payment.finalAmount shouldBe finalAmount
            payment.status shouldBe OrderStatus.PENDING
            payment.orderedAt shouldBe orderedAt
        }

        test("취소된 결제 정보를 생성할 수 있다") {
            // Given
            val orderId = 3L
            val orderNumber = "ORDER-20240101-003"
            val userId = 3L
            val finalAmount = 30000
            val status = OrderStatus.CANCELLED
            val orderedAt = LocalDateTime.now()

            // When
            val payment = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            // Then
            payment.orderId shouldBe orderId
            payment.orderNumber shouldBe orderNumber
            payment.userId shouldBe userId
            payment.finalAmount shouldBe finalAmount
            payment.status shouldBe OrderStatus.CANCELLED
            payment.orderedAt shouldBe orderedAt
        }

        test("금액이 0인 결제 정보를 생성할 수 있다") {
            // Given
            val orderId = 4L
            val orderNumber = "ORDER-20240101-004"
            val userId = 4L
            val finalAmount = 0
            val status = OrderStatus.COMPLETED
            val orderedAt = LocalDateTime.now()

            // When
            val payment = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            // Then
            payment.orderId shouldBe orderId
            payment.orderNumber shouldBe orderNumber
            payment.userId shouldBe userId
            payment.finalAmount shouldBe finalAmount
            payment.status shouldBe status
            payment.orderedAt shouldBe orderedAt
        }

        test("큰 금액의 결제 정보를 생성할 수 있다") {
            // Given
            val orderId = 5L
            val orderNumber = "ORDER-20240101-005"
            val userId = 5L
            val finalAmount = 999_999
            val status = OrderStatus.COMPLETED
            val orderedAt = LocalDateTime.now()

            // When
            val payment = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            // Then
            payment.orderId shouldBe orderId
            payment.orderNumber shouldBe orderNumber
            payment.userId shouldBe userId
            payment.finalAmount shouldBe finalAmount
            payment.status shouldBe status
            payment.orderedAt shouldBe orderedAt
        }
    }

    context("Payment 불변성 테스트") {
        
        test("Payment 객체는 불변 객체이다") {
            // Given
            val orderId = 1L
            val orderNumber = "ORDER-20240101-001"
            val userId = 1L
            val finalAmount = 25000
            val status = OrderStatus.COMPLETED
            val orderedAt = LocalDateTime.now()

            val payment1 = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            val payment2 = Payment(
                orderId = orderId,
                orderNumber = orderNumber,
                userId = userId,
                finalAmount = finalAmount,
                status = status,
                orderedAt = orderedAt
            )

            // When & Then
            payment1 shouldBe payment2
            payment1.hashCode() shouldBe payment2.hashCode()
        }

        test("서로 다른 Payment 객체는 동등하지 않다") {
            // Given
            val orderedAt = LocalDateTime.now()

            val payment1 = Payment(
                orderId = 1L,
                orderNumber = "ORDER-20240101-001",
                userId = 1L,
                finalAmount = 25000,
                status = OrderStatus.COMPLETED,
                orderedAt = orderedAt
            )

            val payment2 = Payment(
                orderId = 2L,
                orderNumber = "ORDER-20240101-002",
                userId = 1L,
                finalAmount = 25000,
                status = OrderStatus.COMPLETED,
                orderedAt = orderedAt
            )

            // When & Then
            (payment1 == payment2) shouldBe false
        }
    }

    context("Payment copy 테스트") {
        
        test("Payment 객체를 copy하여 일부 필드를 변경할 수 있다") {
            // Given
            val originalPayment = Payment(
                orderId = 1L,
                orderNumber = "ORDER-20240101-001",
                userId = 1L,
                finalAmount = 25000,
                status = OrderStatus.PENDING,
                orderedAt = LocalDateTime.now()
            )

            // When
            val completedPayment = originalPayment.copy(status = OrderStatus.COMPLETED)

            // Then
            completedPayment.orderId shouldBe originalPayment.orderId
            completedPayment.orderNumber shouldBe originalPayment.orderNumber
            completedPayment.userId shouldBe originalPayment.userId
            completedPayment.finalAmount shouldBe originalPayment.finalAmount
            completedPayment.status shouldBe OrderStatus.COMPLETED
            completedPayment.orderedAt shouldBe originalPayment.orderedAt
        }

        test("Payment 객체를 copy하여 금액을 변경할 수 있다") {
            // Given
            val originalPayment = Payment(
                orderId = 1L,
                orderNumber = "ORDER-20240101-001",
                userId = 1L,
                finalAmount = 25000,
                status = OrderStatus.PENDING,
                orderedAt = LocalDateTime.now()
            )

            val newAmount = 30000

            // When
            val updatedPayment = originalPayment.copy(finalAmount = newAmount)

            // Then
            updatedPayment.orderId shouldBe originalPayment.orderId
            updatedPayment.orderNumber shouldBe originalPayment.orderNumber
            updatedPayment.userId shouldBe originalPayment.userId
            updatedPayment.finalAmount shouldBe newAmount
            updatedPayment.status shouldBe originalPayment.status
            updatedPayment.orderedAt shouldBe originalPayment.orderedAt
        }
    }

    context("Payment toString 테스트") {
        
        test("Payment 객체의 문자열 표현을 확인할 수 있다") {
            // Given
            val payment = Payment(
                orderId = 1L,
                orderNumber = "ORDER-20240101-001",
                userId = 1L,
                finalAmount = 25000,
                status = OrderStatus.COMPLETED,
                orderedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
            )

            // When
            val paymentString = payment.toString()

            // Then
            paymentString.contains("orderId=1") shouldBe true
            paymentString.contains("orderNumber=ORDER-20240101-001") shouldBe true
            paymentString.contains("userId=1") shouldBe true
            paymentString.contains("finalAmount=25000") shouldBe true
            paymentString.contains("status=COMPLETED") shouldBe true
        }
    }
})
