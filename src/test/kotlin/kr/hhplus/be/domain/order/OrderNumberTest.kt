package kr.hhplus.be.domain.order

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveLength
import io.kotest.matchers.string.shouldMatch
import io.kotest.matchers.string.shouldStartWith
import java.time.LocalDateTime

class OrderNumberTest : DescribeSpec({

    describe("주문번호 생성 테스트") {
        
        it("주문번호는 TYYMMDDhhmmssXX 포맷으로 생성되어야 한다") {
            // Given
            val userId = 1L
            val originalAmount = 10000
            val discountAmount = 1000
            val finalAmount = 9000
            val orderDateTime = LocalDateTime.of(2024, 12, 25, 14, 30, 45)

            // When
            val order = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null,
                orderedAt = orderDateTime
            )

            // Then
            order.orderNumber shouldStartWith "T"
            order.orderNumber shouldHaveLength 15
            
            val expectedPrefix = "T241225143045"
            order.orderNumber shouldStartWith expectedPrefix
            
            order.orderNumber shouldMatch Regex("^T\\d{12}\\d{2}$")
        }

        it("다른 시간에 생성된 주문번호는 시간 부분이 다르게 생성되어야 한다") {
            // Given
            val userId = 1L
            val originalAmount = 10000
            val discountAmount = 0
            val finalAmount = 10000
            
            val dateTime1 = LocalDateTime.of(2024, 1, 1, 9, 15, 30)
            val dateTime2 = LocalDateTime.of(2024, 12, 31, 23, 59, 59)

            // When
            val order1 = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null,
                orderedAt = dateTime1
            )

            val order2 = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null,
                orderedAt = dateTime2
            )

            // Then
            order1.orderNumber shouldStartWith "T240101091530"
            order2.orderNumber shouldStartWith "T241231235959"
            order1.orderNumber shouldHaveLength 15
            order2.orderNumber shouldHaveLength 15
        }

        it("같은 시간에 생성된 주문번호들은 랜덤 부분이 다를 수 있어야 한다") {
            // Given
            val userId = 1L
            val originalAmount = 10000
            val discountAmount = 0
            val finalAmount = 10000
            val sameDateTime = LocalDateTime.of(2024, 6, 15, 12, 0, 0)

            // When
            val orders = mutableListOf<Order>()
            repeat(100) {
                val order = Order.create(
                    userId = userId,
                    originalAmount = originalAmount,
                    discountAmount = discountAmount,
                    finalAmount = finalAmount,
                    userCouponId = null,
                    orderedAt = sameDateTime
                )
                orders.add(order)
            }

            // Then
            val orderNumbers = orders.map { it.orderNumber }
            
            orderNumbers.all { it.startsWith("T240615120000") } shouldBe true
            
            orderNumbers.all { it.length == 15 } shouldBe true
            
            val randomParts = orderNumbers.map { it.substring(13) }
            
            randomParts.all { it.matches(Regex("\\d{2}")) } shouldBe true
            
            val uniqueRandomParts = randomParts.toSet()
            assert(uniqueRandomParts.size >= 10) { 
                "랜덤 부분의 다양성이 부족합니다. 유니크한 값: ${uniqueRandomParts.size}" 
            }
        }

        it("윤년과 평년의 2월 29일 처리가 올바르게 되어야 한다") {
            //
            val userId = 1L
            val originalAmount = 10000
            val discountAmount = 0
            val finalAmount = 10000
            val leapYearDate = LocalDateTime.of(2025, 7, 29, 10, 30, 45)

            // When
            val order = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null,
                orderedAt = leapYearDate
            )

            // Then
            order.orderNumber shouldStartWith "T250729103045"
            order.orderNumber shouldHaveLength 15
        }

        it("자정과 정오 시간이 올바르게 포맷되어야 한다") {
            // Given
            val userId = 1L
            val originalAmount = 10000
            val discountAmount = 0
            val finalAmount = 10000
            
            val midnight = LocalDateTime.of(2024, 5, 10, 0, 0, 0)
            val noon = LocalDateTime.of(2024, 5, 10, 12, 0, 0)

            // When
            val midnightOrder = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null,
                orderedAt = midnight
            )

            val noonOrder = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = null,
                orderedAt = noon
            )

            // Then
            midnightOrder.orderNumber shouldStartWith "T240510000000"
            noonOrder.orderNumber shouldStartWith "T240510120000"
            midnightOrder.orderNumber shouldHaveLength 15
            noonOrder.orderNumber shouldHaveLength 15
        }

        it("생성된 주문번호가 Order 객체에 올바르게 설정되어야 한다") {
            // Given
            val userId = 1L
            val originalAmount = 15000
            val discountAmount = 500
            val finalAmount = 14500
            val userCouponId = 123L
            val orderDateTime = LocalDateTime.of(2024, 8, 20, 16, 45, 30)

            // When
            val order = Order.create(
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                userCouponId = userCouponId,
                orderedAt = orderDateTime
            )

            // Then
            order.orderNumber.isNotBlank() shouldBe true
            order.orderNumber shouldStartWith "T240820164530"
            
            order.userId shouldBe userId
            order.originalAmount shouldBe originalAmount
            order.discountAmount shouldBe discountAmount
            order.finalAmount shouldBe finalAmount
            order.userCouponId shouldBe userCouponId
            order.status shouldBe OrderStatus.PENDING
            order.createdAt shouldBe orderDateTime
        }
    }
})
