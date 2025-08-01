package kr.hhplus.be.domain.product.events

import kr.hhplus.be.domain.product.StockChangeType

data class StockChangedEvent(
    val productId: Long,
    val changeType: StockChangeType,
    val changeQuantity: Int,
    val previousStock: Int,
    val currentStock: Int,
    val reason: String
)
