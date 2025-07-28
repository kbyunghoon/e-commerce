package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.TransactionType
import java.time.LocalDateTime

@Entity
@Table(name = "balance_history")
class BalanceHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "balance_history_id")
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "before_amount", nullable = false)
    val beforeAmount: Int,

    @Column(name = "after_amount", nullable = false)
    val afterAmount: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: TransactionType,

    @Column(name = "transaction_at", nullable = false)
    val transactionAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): BalanceHistory {
        return BalanceHistory(
            id = id,
            userId = userId,
            amount = amount,
            beforeAmount = beforeAmount,
            afterAmount = afterAmount,
            type = type,
        )
    }
}