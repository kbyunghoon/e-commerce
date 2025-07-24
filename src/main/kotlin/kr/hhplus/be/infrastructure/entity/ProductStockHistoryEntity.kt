package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "product_stock_history")
class ProductStockHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val historyId: Long = 0,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "change_quantity", nullable = false)
    val changeQuantity: Int,

    @Column(name = "order_id", nullable = false, unique = true)
    val orderId: Long,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
}