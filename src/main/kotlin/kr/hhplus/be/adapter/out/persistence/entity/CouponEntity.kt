package kr.hhplus.be.adapter.out.persistence.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.model.BalanceChargeHistory
import java.time.LocalDateTime

@Entity
@Table(name = "balance_charge_history")
class BalanceChargeHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val historyId: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "amount", nullable = false)
    val amount: Int,

    @Column(name = "charged_at", nullable = false)
    val chargedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): BalanceChargeHistory {
        return BalanceChargeHistory(
            historyId = this.historyId,
            userId = this.userId,
            amount = this.amount,
            chargedAt = this.chargedAt
        )
    }

    companion object {
        fun fromDomain(history: BalanceChargeHistory): BalanceChargeHistoryEntity {
            return BalanceChargeHistoryEntity(
                historyId = history.historyId,
                userId = history.userId,
                amount = history.amount,
                chargedAt = history.chargedAt
            )
        }
    }
}
