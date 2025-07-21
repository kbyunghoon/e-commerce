package kr.hhplus.be.domain.model

import java.time.LocalDateTime

data class BalanceChargeHistory(
    val historyId: Long = 0,
    val userId: Long,
    val amount: Int,
    val chargedAt: LocalDateTime = LocalDateTime.now()
)