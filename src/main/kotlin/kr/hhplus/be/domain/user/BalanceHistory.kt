package kr.hhplus.be.domain.user

import kr.hhplus.be.application.balance.BalanceHistoryCommand
import java.time.LocalDateTime

data class BalanceHistory(
    val id: Long? = null,
    val userId: Long,
    val amount: Int,
    val beforeAmount: Int,
    val afterAmount: Int,
    val type: TransactionType,
    val transactionAt: LocalDateTime
) {
    companion object {
        fun from(command: BalanceHistoryCommand): BalanceHistory {
            return BalanceHistory(
                userId = command.userId,
                amount = command.amount,
                beforeAmount = command.beforeAmount,
                afterAmount = command.afterAmount,
                type = command.type,
                transactionAt = command.transactionAt
            )
        }
    }
}