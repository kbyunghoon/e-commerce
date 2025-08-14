package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product_price_history")
class ProductPriceHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    val id: Long = 0,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "old_price", nullable = false)
    val oldPrice: Int,

    @Column(name = "new_price", nullable = false)
    val newPrice: Int,

    @Column(name = "reason", nullable = false)
    val reason: String,

    @Column(name = "changed_at", nullable = false, updatable = false)
    val changedAt: LocalDateTime = LocalDateTime.now(),
) {
}