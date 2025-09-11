package kr.hhplus.be.application.product

import kr.hhplus.be.domain.product.StockChangeType
import java.time.LocalDateTime

data class ProductStockHistoryCommand(
    val productId: Long,
    val changeType: StockChangeType,
    val changeQuantity: Int,
    val previousStock: Int,
    val transactionAt: LocalDateTime,
)