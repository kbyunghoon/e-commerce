package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.BalanceHistoryRepository
import kr.hhplus.be.domain.user.TransactionType
import kr.hhplus.be.domain.user.events.BalanceChargedEvent
import kr.hhplus.be.domain.user.events.BalanceDeductedEvent
import kr.hhplus.be.domain.user.events.BalanceRefundedEvent
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
        
        val event = BalanceChargedEvent(
            _userId = userId,
            _beforeAmount = beforeAmount,
            _afterAmount = afterAmount,
            _chargedAmount = chargedAmount,
            _chargedAt = chargedAt
        )

        When("잔액 충전 이벤트가 발행되면") {
            val historySlot = slot<BalanceHistory>()
            every { balanceHistoryRepository.save(capture(historySlot)) } answers { it.invocation.args[0] as BalanceHistory }

            balanceHistoryService.handleBalanceCharged(event)

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
        
        val event = BalanceDeductedEvent(
            _userId = userId,
            _beforeAmount = beforeAmount,
            _afterAmount = afterAmount,
            _deductedAmount = deductedAmount,
            _deductedAt = deductedAt
        )

        When("잔액 차감 이벤트가 발행되면") {
            val historySlot = slot<BalanceHistory>()
            every { balanceHistoryRepository.save(capture(historySlot)) } answers { it.invocation.args[0] as BalanceHistory }

            balanceHistoryService.handleBalanceDeducted(event)

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
        
        val event = BalanceRefundedEvent(
            _userId = userId,
            _beforeAmount = beforeAmount,
            _afterAmount = afterAmount,
            _refundedAmount = refundedAmount,
            _refundedAt = refundedAt
        )

        When("잔액 환불 이벤트가 발행되면") {
            val historySlot = slot<BalanceHistory>()
            every { balanceHistoryRepository.save(capture(historySlot)) } answers { it.invocation.args[0] as BalanceHistory }

            balanceHistoryService.handleBalanceRefunded(event)

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
