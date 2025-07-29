package kr.hhplus.be.domain.user

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime

class BalanceHistoryTest : FunSpec({

    context("BalanceHistory 생성 테스트") {
        
        test("모든 필수 정보를 제공하여 BalanceHistory를 생성할 수 있다") {
            // Given
            val userId = 1L
            val amount = 10000
            val beforeAmount = 5000
            val afterAmount = 15000
            val transactionType = TransactionType.CHARGE
            val transactionAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)

            // When
            val balanceHistory = BalanceHistory(
                userId = userId,
                amount = amount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = transactionType,
                transactionAt = transactionAt
            )

            // Then
            balanceHistory.userId shouldBe userId
            balanceHistory.amount shouldBe amount
            balanceHistory.beforeAmount shouldBe beforeAmount
            balanceHistory.afterAmount shouldBe afterAmount
            balanceHistory.type shouldBe transactionType
            balanceHistory.transactionAt shouldBe transactionAt
        }

        test("id를 지정하지 않으면 기본값 0으로 설정된다") {
            // Given & When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE
            )

            // Then
            balanceHistory.id shouldBe 0
        }

        test("transactionAt을 지정하지 않으면 현재 시간으로 자동 설정된다") {
            // Given
            val beforeCreation = LocalDateTime.now().minusSeconds(1)
            
            // When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE
            )
            
            val afterCreation = LocalDateTime.now().plusSeconds(1)

            // Then
            balanceHistory.transactionAt shouldNotBe null
            balanceHistory.transactionAt.isAfter(beforeCreation) shouldBe true
            balanceHistory.transactionAt.isBefore(afterCreation) shouldBe true
        }

        test("충전 타입의 잔액 히스토리를 생성할 수 있다") {
            // Given & When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE
            )

            // Then
            balanceHistory.type shouldBe TransactionType.CHARGE
            balanceHistory.amount shouldBe 10000
            balanceHistory.beforeAmount shouldBe 5000
            balanceHistory.afterAmount shouldBe 15000
        }

        test("차감 타입의 잔액 히스토리를 생성할 수 있다") {
            // Given & When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = -3000,
                beforeAmount = 10000,
                afterAmount = 7000,
                type = TransactionType.DEDUCT
            )

            // Then
            balanceHistory.type shouldBe TransactionType.DEDUCT
            balanceHistory.amount shouldBe -3000
            balanceHistory.beforeAmount shouldBe 10000
            balanceHistory.afterAmount shouldBe 7000
        }

        test("환불 타입의 잔액 히스토리를 생성할 수 있다") {
            // Given & When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = 5000,
                beforeAmount = 2000,
                afterAmount = 7000,
                type = TransactionType.REFUND
            )

            // Then
            balanceHistory.type shouldBe TransactionType.REFUND
            balanceHistory.amount shouldBe 5000
            balanceHistory.beforeAmount shouldBe 2000
            balanceHistory.afterAmount shouldBe 7000
        }
    }

    context("BalanceHistory 데이터 검증 테스트") {
        
        test("금액 계산이 올바른지 확인할 수 있다 - 충전의 경우") {
            // Given
            val beforeAmount = 5000
            val chargeAmount = 10000
            val afterAmount = 15000

            // When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = chargeAmount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = TransactionType.CHARGE
            )

            // Then
            balanceHistory.beforeAmount + balanceHistory.amount shouldBe balanceHistory.afterAmount
        }

        test("금액 계산이 올바른지 확인할 수 있다 - 차감의 경우") {
            // Given
            val beforeAmount = 10000
            val deductAmount = -3000
            val afterAmount = 7000

            // When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = deductAmount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = TransactionType.DEDUCT
            )

            // Then
            balanceHistory.beforeAmount + balanceHistory.amount shouldBe balanceHistory.afterAmount
        }

        test("금액 계산이 올바른지 확인할 수 있다 - 환불의 경우") {
            // Given
            val beforeAmount = 2000
            val refundAmount = 5000
            val afterAmount = 7000

            // When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = refundAmount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = TransactionType.REFUND
            )

            // Then
            balanceHistory.beforeAmount + balanceHistory.amount shouldBe balanceHistory.afterAmount
        }

        test("음수 사용자 ID로도 히스토리를 생성할 수 있다") {
            // Given & When
            val balanceHistory = BalanceHistory(
                userId = -1L,
                amount = 10000,
                beforeAmount = 0,
                afterAmount = 10000,
                type = TransactionType.CHARGE
            )

            // Then
            balanceHistory.userId shouldBe -1L
        }

        test("0원 거래 히스토리를 생성할 수 있다") {
            // Given & When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = 0,
                beforeAmount = 5000,
                afterAmount = 5000,
                type = TransactionType.CHARGE
            )

            // Then
            balanceHistory.amount shouldBe 0
            balanceHistory.beforeAmount shouldBe balanceHistory.afterAmount
        }

        test("매우 큰 금액의 히스토리를 생성할 수 있다") {
            // Given
            val largeAmount = Int.MAX_VALUE

            // When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = largeAmount,
                beforeAmount = 0,
                afterAmount = largeAmount,
                type = TransactionType.CHARGE
            )

            // Then
            balanceHistory.amount shouldBe largeAmount
        }

        test("매우 작은 음수 금액의 히스토리를 생성할 수 있다") {
            // Given
            val smallAmount = Int.MIN_VALUE

            // When
            val balanceHistory = BalanceHistory(
                userId = 1L,
                amount = smallAmount,
                beforeAmount = 0,
                afterAmount = smallAmount,
                type = TransactionType.DEDUCT
            )

            // Then
            balanceHistory.amount shouldBe smallAmount
        }
    }

    context("BalanceHistory 불변성 테스트") {
        
        test("BalanceHistory 객체는 불변 객체이다") {
            // Given
            val userId = 1L
            val amount = 10000
            val beforeAmount = 5000
            val afterAmount = 15000
            val transactionType = TransactionType.CHARGE
            val transactionAt = LocalDateTime.now()

            val balanceHistory1 = BalanceHistory(
                userId = userId,
                amount = amount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = transactionType,
                transactionAt = transactionAt
            )

            val balanceHistory2 = BalanceHistory(
                userId = userId,
                amount = amount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = transactionType,
                transactionAt = transactionAt
            )

            // When & Then
            balanceHistory1 shouldBe balanceHistory2
            balanceHistory1.hashCode() shouldBe balanceHistory2.hashCode()
        }

        test("서로 다른 BalanceHistory 객체는 동등하지 않다") {
            // Given
            val transactionAt = LocalDateTime.now()

            val balanceHistory1 = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE,
                transactionAt = transactionAt
            )

            val balanceHistory2 = BalanceHistory(
                userId = 2L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE,
                transactionAt = transactionAt
            )

            // When & Then
            (balanceHistory1 == balanceHistory2) shouldBe false
        }
    }

    context("BalanceHistory copy 테스트") {
        
        test("BalanceHistory 객체를 copy하여 일부 필드를 변경할 수 있다") {
            // Given
            val originalHistory = BalanceHistory(
                id = 1L,
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE,
                transactionAt = LocalDateTime.now()
            )

            // When
            val modifiedHistory = originalHistory.copy(userId = 2L)

            // Then
            modifiedHistory.id shouldBe originalHistory.id
            modifiedHistory.userId shouldBe 2L
            modifiedHistory.amount shouldBe originalHistory.amount
            modifiedHistory.beforeAmount shouldBe originalHistory.beforeAmount
            modifiedHistory.afterAmount shouldBe originalHistory.afterAmount
            modifiedHistory.type shouldBe originalHistory.type
            modifiedHistory.transactionAt shouldBe originalHistory.transactionAt
        }

        test("BalanceHistory 객체를 copy하여 거래 타입을 변경할 수 있다") {
            // Given
            val originalHistory = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE,
                transactionAt = LocalDateTime.now()
            )

            // When
            val modifiedHistory = originalHistory.copy(type = TransactionType.REFUND)

            // Then
            modifiedHistory.userId shouldBe originalHistory.userId
            modifiedHistory.amount shouldBe originalHistory.amount
            modifiedHistory.beforeAmount shouldBe originalHistory.beforeAmount
            modifiedHistory.afterAmount shouldBe originalHistory.afterAmount
            modifiedHistory.type shouldBe TransactionType.REFUND
            modifiedHistory.transactionAt shouldBe originalHistory.transactionAt
        }

        test("BalanceHistory 객체를 copy하여 금액들을 변경할 수 있다") {
            // Given
            val originalHistory = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE,
                transactionAt = LocalDateTime.now()
            )

            val newAmount = 20000
            val newBeforeAmount = 10000
            val newAfterAmount = 30000

            // When
            val modifiedHistory = originalHistory.copy(
                amount = newAmount,
                beforeAmount = newBeforeAmount,
                afterAmount = newAfterAmount
            )

            // Then
            modifiedHistory.userId shouldBe originalHistory.userId
            modifiedHistory.amount shouldBe newAmount
            modifiedHistory.beforeAmount shouldBe newBeforeAmount
            modifiedHistory.afterAmount shouldBe newAfterAmount
            modifiedHistory.type shouldBe originalHistory.type
            modifiedHistory.transactionAt shouldBe originalHistory.transactionAt
        }
    }

    context("BalanceHistory toString 테스트") {
        
        test("BalanceHistory 객체의 문자열 표현을 확인할 수 있다") {
            // Given
            val balanceHistory = BalanceHistory(
                id = 1L,
                userId = 1L,
                amount = 10000,
                beforeAmount = 5000,
                afterAmount = 15000,
                type = TransactionType.CHARGE,
                transactionAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
            )

            // When
            val balanceHistoryString = balanceHistory.toString()

            // Then
            balanceHistoryString.contains("id=1") shouldBe true
            balanceHistoryString.contains("userId=1") shouldBe true
            balanceHistoryString.contains("amount=10000") shouldBe true
            balanceHistoryString.contains("beforeAmount=5000") shouldBe true
            balanceHistoryString.contains("afterAmount=15000") shouldBe true
            balanceHistoryString.contains("type=CHARGE") shouldBe true
        }
    }

    context("TransactionType 열거형 테스트") {
        
        test("모든 TransactionType 값들이 정의되어 있다") {
            // When & Then
            TransactionType.CHARGE shouldBe TransactionType.CHARGE
            TransactionType.DEDUCT shouldBe TransactionType.DEDUCT
            TransactionType.REFUND shouldBe TransactionType.REFUND
        }

        test("TransactionType은 올바른 문자열 표현을 가진다") {
            // When & Then
            TransactionType.CHARGE.name shouldBe "CHARGE"
            TransactionType.DEDUCT.name shouldBe "DEDUCT"
            TransactionType.REFUND.name shouldBe "REFUND"
        }

        test("TransactionType 값들은 서로 다르다") {
            // When & Then
            (TransactionType.CHARGE == TransactionType.DEDUCT) shouldBe false
            (TransactionType.CHARGE == TransactionType.REFUND) shouldBe false
            (TransactionType.DEDUCT == TransactionType.REFUND) shouldBe false
        }

        test("TransactionType.values()로 모든 값을 조회할 수 있다") {
            // When
            val values = TransactionType.values()

            // Then
            values.size shouldBe 3
            values.contains(TransactionType.CHARGE) shouldBe true
            values.contains(TransactionType.DEDUCT) shouldBe true
            values.contains(TransactionType.REFUND) shouldBe true
        }

        test("TransactionType.valueOf()로 문자열에서 열거형을 생성할 수 있다") {
            // When & Then
            TransactionType.valueOf("CHARGE") shouldBe TransactionType.CHARGE
            TransactionType.valueOf("DEDUCT") shouldBe TransactionType.DEDUCT
            TransactionType.valueOf("REFUND") shouldBe TransactionType.REFUND
        }
    }

    context("BalanceHistory 실제 사용 시나리오 테스트") {
        
        test("충전 시나리오 - 0원에서 10,000원 충전") {
            // Given & When
            val chargeHistory = BalanceHistory(
                userId = 1L,
                amount = 10000,
                beforeAmount = 0,
                afterAmount = 10000,
                type = TransactionType.CHARGE
            )

            // Then
            chargeHistory.beforeAmount shouldBe 0
            chargeHistory.amount shouldBe 10000
            chargeHistory.afterAmount shouldBe 10000
            chargeHistory.type shouldBe TransactionType.CHARGE
        }

        test("결제 시나리오 - 15,000원에서 5,000원 차감") {
            // Given & When
            val deductHistory = BalanceHistory(
                userId = 1L,
                amount = -5000,
                beforeAmount = 15000,
                afterAmount = 10000,
                type = TransactionType.DEDUCT
            )

            // Then
            deductHistory.beforeAmount shouldBe 15000
            deductHistory.amount shouldBe -5000
            deductHistory.afterAmount shouldBe 10000
            deductHistory.type shouldBe TransactionType.DEDUCT
        }

        test("환불 시나리오 - 10,000원에 3,000원 환불") {
            // Given & When
            val refundHistory = BalanceHistory(
                userId = 1L,
                amount = 3000,
                beforeAmount = 10000,
                afterAmount = 13000,
                type = TransactionType.REFUND
            )

            // Then
            refundHistory.beforeAmount shouldBe 10000
            refundHistory.amount shouldBe 3000
            refundHistory.afterAmount shouldBe 13000
            refundHistory.type shouldBe TransactionType.REFUND
        }

        test("연속 거래 시나리오") {
            // Given
            val initialBalance = 0
            val chargeAmount = 50000
            val firstPurchase = -15000
            val secondPurchase = -8000
            val refundAmount = 5000

            // When
            val chargeHistory = BalanceHistory(
                userId = 1L,
                amount = chargeAmount,
                beforeAmount = initialBalance,
                afterAmount = initialBalance + chargeAmount,
                type = TransactionType.CHARGE
            )

            val firstPurchaseHistory = BalanceHistory(
                userId = 1L,
                amount = firstPurchase,
                beforeAmount = chargeHistory.afterAmount,
                afterAmount = chargeHistory.afterAmount + firstPurchase,
                type = TransactionType.DEDUCT
            )

            val secondPurchaseHistory = BalanceHistory(
                userId = 1L,
                amount = secondPurchase,
                beforeAmount = firstPurchaseHistory.afterAmount,
                afterAmount = firstPurchaseHistory.afterAmount + secondPurchase,
                type = TransactionType.DEDUCT
            )

            val refundHistory = BalanceHistory(
                userId = 1L,
                amount = refundAmount,
                beforeAmount = secondPurchaseHistory.afterAmount,
                afterAmount = secondPurchaseHistory.afterAmount + refundAmount,
                type = TransactionType.REFUND
            )

            // Then
            chargeHistory.afterAmount shouldBe 50000
            firstPurchaseHistory.afterAmount shouldBe 35000
            secondPurchaseHistory.afterAmount shouldBe 27000
            refundHistory.afterAmount shouldBe 32000
        }
    }
})
