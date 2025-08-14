package kr.hhplus.be.domain.user

import java.time.LocalDateTime

data class BalanceHistory(
    val id: Long = 0,
    val userId: Long,
    val amount: Int,
    val beforeAmount: Int,
    val afterAmount: Int,
    val type: TransactionType,
    val transactionAt: LocalDateTime = LocalDateTime.now()
)