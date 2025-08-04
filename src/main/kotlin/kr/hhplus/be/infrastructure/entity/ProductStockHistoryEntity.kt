package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.product.ProductStockHistory
import kr.hhplus.be.domain.product.StockChangeType
import java.time.LocalDateTime

@Entity
@Table(name = "product_stock_history")
class ProductStockHistoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    val id: Long = 0L,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "change_type", nullable = false)
    val changeType: StockChangeType,

    @Column(name = "change_quantity", nullable = false)
    val changeQuantity: Int,

    @Column(name = "previous_stock", nullable = false)
    val previousStock: Int,

    @Column(name = "current_stock", nullable = false)
    val currentStock: Int,

    @Column(name = "reason", nullable = false)
    val reason: String,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): ProductStockHistory {
        return ProductStockHistory(
            id = this.id,
            productId = this.productId,
            changeType = this.changeType,
            changeQuantity = this.changeQuantity,
            previousStock = this.previousStock,
            currentStock = this.currentStock,
            reason = this.reason,
            createdAt = this.createdAt
        )
    }

    companion object {
        fun from(domain: ProductStockHistory): ProductStockHistoryEntity {
            return ProductStockHistoryEntity(
                id = domain.id,
                productId = domain.productId,
                changeType = domain.changeType,
                changeQuantity = domain.changeQuantity,
                previousStock = domain.previousStock,
                currentStock = domain.currentStock,
                reason = domain.reason,
                createdAt = domain.createdAt
            )
        }
    }
}
