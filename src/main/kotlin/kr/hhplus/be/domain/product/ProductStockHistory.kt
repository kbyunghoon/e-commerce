package kr.hhplus.be.domain.product

import java.time.LocalDateTime

data class ProductStockHistory(
    val id: Long? = null,
    val productId: Long,
    val changeType: StockChangeType,
    val changeQuantity: Int,
    val previousStock: Int,
    val currentStock: Int,
    val transactionAt: LocalDateTime
) {
    companion object {
        fun create(
            productId: Long,
            changeType: StockChangeType,
            changeQuantity: Int,
            previousStock: Int,
            transactionAt: LocalDateTime
        ): ProductStockHistory {
            val currentStock = when (changeType) {
                StockChangeType.DEDUCT -> previousStock - changeQuantity
                StockChangeType.RESTORE -> previousStock + changeQuantity
            }

            return ProductStockHistory(
                productId = productId,
                changeType = changeType,
                changeQuantity = changeQuantity,
                previousStock = previousStock,
                currentStock = currentStock,
                transactionAt = transactionAt
            )
        }
    }
}