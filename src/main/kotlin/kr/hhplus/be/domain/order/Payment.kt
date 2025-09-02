package kr.hhplus.be.domain.order

import java.time.LocalDateTime

data class Payment(
    val orderId: Long,
    val orderNumber: String,
    val userId: Long,
    val finalAmount: Int,
    val status: OrderStatus,
    val orderedAt: LocalDateTime
)
