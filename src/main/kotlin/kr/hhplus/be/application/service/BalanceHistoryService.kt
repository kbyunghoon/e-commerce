package kr.hhplus.be.application.service

import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.BalanceHistoryRepository
import kr.hhplus.be.domain.user.TransactionType
import kr.hhplus.be.domain.user.events.BalanceChargedEvent
import kr.hhplus.be.domain.user.events.BalanceDeductedEvent
import kr.hhplus.be.domain.user.events.BalanceRefundedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceHistoryService(
    private val balanceHistoryRepository: BalanceHistoryRepository
) {

    @EventListener
    @Transactional
    fun handleBalanceCharged(event: BalanceChargedEvent) {
        val history = BalanceHistory(
            userId = event.userId,
            amount = event.transactionAmount,
            beforeAmount = event.beforeAmount,
            afterAmount = event.afterAmount,
            type = TransactionType.CHARGE,
            transactionAt = event.transactionAt
        )
        balanceHistoryRepository.save(history)
    }

    @EventListener
    @Transactional
    fun handleBalanceDeducted(event: BalanceDeductedEvent) {
        val history = BalanceHistory(
            userId = event.userId,
            amount = event.transactionAmount,
            beforeAmount = event.beforeAmount,
            afterAmount = event.afterAmount,
            type = TransactionType.DEDUCT,
            transactionAt = event.transactionAt
        )
        balanceHistoryRepository.save(history)
    }

    @EventListener
    @Transactional
    fun handleBalanceRefunded(event: BalanceRefundedEvent) {
        val history = BalanceHistory(
            userId = event.userId,
            amount = event.transactionAmount,
            beforeAmount = event.beforeAmount,
            afterAmount = event.afterAmount,
            type = TransactionType.REFUND,
            transactionAt = event.transactionAt
        )
        balanceHistoryRepository.save(history)
    }
}
