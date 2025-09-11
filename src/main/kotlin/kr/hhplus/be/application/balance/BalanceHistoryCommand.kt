package kr.hhplus.be.application.balance

import kr.hhplus.be.domain.user.TransactionType
import java.time.LocalDateTime

data class BalanceHistoryCommand(
    val userId: Long,
    val amount: Int,
    val beforeAmount: Int,
    val afterAmount: Int,
    val type: TransactionType,
    val transactionAt: LocalDateTime = LocalDateTime.now()
)