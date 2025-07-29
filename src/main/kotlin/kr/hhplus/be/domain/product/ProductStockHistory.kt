package kr.hhplus.be.domain.product

import kr.hhplus.be.infrastructure.entity.ProductStockHistoryEntity
import java.time.LocalDateTime

data class ProductStockHistory(
    val id: Long = 0L,
    val productId: Long,
    val changeType: StockChangeType,
    val changeQuantity: Int,
    val previousStock: Int,
    val currentStock: Int,
    val reason: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun toEntity(): ProductStockHistoryEntity {
        return ProductStockHistoryEntity(
            id = this.id,
            productId = productId,
            changeType = this.changeType,
            changeQuantity = this.changeQuantity,
            previousStock = this.previousStock,
            currentStock = this.currentStock,
            reason = this.reason,
            createdAt = this.createdAt
        )
    }
}
