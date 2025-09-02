package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.*
import kr.hhplus.be.application.balance.BalanceHistoryCommand
import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.BalanceHistoryRepository
import kr.hhplus.be.domain.user.TransactionType
import java.time.LocalDateTime

class BalanceHistoryServiceTest : BehaviorSpec({
    val balanceHistoryRepository: BalanceHistoryRepository = mockk()
    val balanceHistoryService = BalanceHistoryService(balanceHistoryRepository)

    afterContainer {
        clearAllMocks()
    }

    Given("잔액 충전 이벤트 처리 시나리오") {
        val userId = 1L
        val beforeAmount = 10000
        val afterAmount = 15000
        val chargedAmount = 5000
        val chargedAt = LocalDateTime.now()

        val command = BalanceHistoryCommand(
            userId = userId,
            beforeAmount = beforeAmount,
            afterAmount = afterAmount,
            amount = chargedAmount,
            type = TransactionType.CHARGE,
            transactionAt = chargedAt
        )

        When("잔액 충전 이벤트가 발행되면") {
            val historySlot = slot<BalanceHistory>()
            every { balanceHistoryRepository.save(capture(historySlot)) } answers { it.invocation.args[0] as BalanceHistory }

            balanceHistoryService.save(command)

            Then("CHARGE 타입의 히스토리가 저장된다") {
                verify(exactly = 1) { balanceHistoryRepository.save(any()) }

                val savedHistory = historySlot.captured
                savedHistory.userId shouldBe userId
                savedHistory.amount shouldBe chargedAmount
                savedHistory.beforeAmount shouldBe beforeAmount
                savedHistory.afterAmount shouldBe afterAmount
                savedHistory.type shouldBe TransactionType.CHARGE
                savedHistory.transactionAt shouldBe chargedAt
            }
        }
    }

    Given("잔액 차감 이벤트 처리 시나리오") {
        val userId = 1L
        val beforeAmount = 15000
        val afterAmount = 10000
        val deductedAmount = 5000
        val deductedAt = LocalDateTime.now()

        val command = BalanceHistoryCommand(
            userId = userId,
            amount = deductedAmount,
            beforeAmount = beforeAmount,
            afterAmount = afterAmount,
            type = TransactionType.DEDUCT,
            transactionAt = deductedAt
        )

        When("잔액 차감 이벤트가 발행되면") {
            val historySlot = slot<BalanceHistory>()
            every { balanceHistoryRepository.save(capture(historySlot)) } answers { it.invocation.args[0] as BalanceHistory }

            balanceHistoryService.save(command)

            Then("DEDUCT 타입의 히스토리가 저장된다") {
                verify(exactly = 1) { balanceHistoryRepository.save(any()) }

                val savedHistory = historySlot.captured
                savedHistory.userId shouldBe userId
                savedHistory.amount shouldBe deductedAmount
                savedHistory.beforeAmount shouldBe beforeAmount
                savedHistory.afterAmount shouldBe afterAmount
                savedHistory.type shouldBe TransactionType.DEDUCT
                savedHistory.transactionAt shouldBe deductedAt
            }
        }
    }

    Given("잔액 환불 이벤트 처리 시나리오") {
        val userId = 1L
        val beforeAmount = 10000
        val afterAmount = 15000
        val refundedAmount = 5000
        val refundedAt = LocalDateTime.now()

        val command = BalanceHistoryCommand(
            userId = userId,
            amount = refundedAmount,
            beforeAmount = beforeAmount,
            afterAmount = afterAmount,
            type = TransactionType.REFUND,
            transactionAt = refundedAt
        )

        When("잔액 환불 이벤트가 발행되면") {
            val historySlot = slot<BalanceHistory>()
            every { balanceHistoryRepository.save(capture(historySlot)) } answers { it.invocation.args[0] as BalanceHistory }

            balanceHistoryService.save(command)

            Then("REFUND 타입의 히스토리가 저장된다") {
                verify(exactly = 1) { balanceHistoryRepository.save(any()) }

                val savedHistory = historySlot.captured
                savedHistory.userId shouldBe userId
                savedHistory.amount shouldBe refundedAmount
                savedHistory.beforeAmount shouldBe beforeAmount
                savedHistory.afterAmount shouldBe afterAmount
                savedHistory.type shouldBe TransactionType.REFUND
                savedHistory.transactionAt shouldBe refundedAt
            }
        }
    }
})
