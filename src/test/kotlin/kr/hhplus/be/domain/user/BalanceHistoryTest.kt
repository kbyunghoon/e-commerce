package kr.hhplus.be.domain.user

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDateTime

class BalanceHistoryTest : BehaviorSpec({

    Given("BalanceHistory 객체 생성 시나리오") {
        val userId = 1L
        val amount = 10000
        val beforeAmount = 5000
        val afterAmount = 15000
        val transactionType = TransactionType.CHARGE
        val transactionAt = LocalDateTime.now()

        When("모든 필수 정보를 제공하여 BalanceHistory를 생성하면") {
            val balanceHistory = BalanceHistory(
                userId = userId,
                amount = amount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = transactionType,
                transactionAt = transactionAt
            )

            Then("BalanceHistory 객체가 성공적으로 생성되고, 제공된 값들을 포함한다") {
                balanceHistory.userId shouldBe userId
                balanceHistory.amount shouldBe amount
                balanceHistory.beforeAmount shouldBe beforeAmount
                balanceHistory.afterAmount shouldBe afterAmount
                balanceHistory.type shouldBe transactionType
                balanceHistory.transactionAt shouldBe transactionAt
            }
        }

        When("id를 지정하지 않고 BalanceHistory를 생성하면") {
            val balanceHistory = BalanceHistory(
                userId = userId,
                amount = amount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = transactionType,
                transactionAt = transactionAt
            )

            Then("id가 기본값인 0으로 설정된다") {
                balanceHistory.id shouldBe 0
            }
        }

        When("transactionAt을 지정하지 않고 BalanceHistory를 생성하면") {
            val balanceHistory = BalanceHistory(
                userId = userId,
                amount = amount,
                beforeAmount = beforeAmount,
                afterAmount = afterAmount,
                type = transactionType
            )

            Then("transactionAt이 현재 시간으로 자동 설정된다") {
                balanceHistory.transactionAt.withNano(0) shouldBe LocalDateTime.now().withNano(0)
            }
        }
    }

    Given("BalanceHistory toEntity() 변환 시나리오") {
        val userId = 1L
        val amount = 10000
        val beforeAmount = 5000
        val afterAmount = 15000
        val transactionType = TransactionType.CHARGE
        val transactionAt = LocalDateTime.now()

        val balanceHistory = BalanceHistory(
            id = 1L,
            userId = userId,
            amount = amount,
            beforeAmount = beforeAmount,
            afterAmount = afterAmount,
            type = transactionType,
            transactionAt = transactionAt
        )

        When("toEntity() 메소드를 호출하면") {
            val entity = balanceHistory.toEntity()

            Then("BalanceHistoryEntity 객체가 성공적으로 생성되고, 동일한 값들을 포함한다") {
                entity.id shouldBe balanceHistory.id
                entity.userId shouldBe balanceHistory.userId
                entity.amount shouldBe balanceHistory.amount
                entity.beforeAmount shouldBe balanceHistory.beforeAmount
                entity.afterAmount shouldBe balanceHistory.afterAmount
                entity.type shouldBe balanceHistory.type
                entity.transactionAt shouldBe balanceHistory.transactionAt
            }
        }
    }
})