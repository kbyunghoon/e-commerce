package kr.hhplus.be.domain.product

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
)