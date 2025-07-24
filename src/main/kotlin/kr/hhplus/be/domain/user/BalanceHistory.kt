package kr.hhplus.be.domain.user

import kr.hhplus.be.infrastructure.entity.BalanceHistoryEntity
import java.time.LocalDateTime

data class BalanceHistory(
    val id: Long = 0,
    val userId: Long,
    val amount: Int,
    val beforeAmount: Int,
    val afterAmount: Int,
    val type: TransactionType,
    val transactionAt: LocalDateTime = LocalDateTime.now()
) {
    fun toEntity(): BalanceHistoryEntity {
        return BalanceHistoryEntity(
            id = this.id,
            userId = this.userId,
            amount = this.amount,
            beforeAmount = this.beforeAmount,
            afterAmount = this.afterAmount,
            type = this.type,
            transactionAt = this.transactionAt
        )
    }
}