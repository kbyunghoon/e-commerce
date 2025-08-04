package kr.hhplus.be.domain.user.events

import java.time.LocalDateTime

/**
 * 잔액 관련 도메인 이벤트들
 */
sealed class BalanceEvent(
    val userId: Long,
    val beforeAmount: Int,
    val afterAmount: Int,
    val transactionAmount: Int,
    val transactionAt: LocalDateTime
)

data class BalanceChargedEvent(
    private val _userId: Long,
    private val _beforeAmount: Int,
    private val _afterAmount: Int,
    private val _chargedAmount: Int,
    private val _chargedAt: LocalDateTime = LocalDateTime.now()
) : BalanceEvent(_userId, _beforeAmount, _afterAmount, _chargedAmount, _chargedAt)

data class BalanceDeductedEvent(
    private val _userId: Long,
    private val _beforeAmount: Int,
    private val _afterAmount: Int,
    private val _deductedAmount: Int,
    private val _deductedAt: LocalDateTime = LocalDateTime.now()
) : BalanceEvent(_userId, _beforeAmount, _afterAmount, _deductedAmount, _deductedAt)

data class BalanceRefundedEvent(
    private val _userId: Long,
    private val _beforeAmount: Int,
    private val _afterAmount: Int,
    private val _refundedAmount: Int,
    private val _refundedAt: LocalDateTime = LocalDateTime.now()
) : BalanceEvent(_userId, _beforeAmount, _afterAmount, _refundedAmount, _refundedAt)
