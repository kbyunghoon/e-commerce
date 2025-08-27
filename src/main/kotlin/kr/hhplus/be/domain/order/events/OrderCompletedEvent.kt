package kr.hhplus.be.domain.order.events

import java.time.LocalDateTime

data class OrderCompletedEvent(
    val orderId: Long,
    val userId: Long,
    val totalAmount: Int,
    val finalAmount: Int,
    val discountAmount: Int,
    val userCouponId: Long?,
    val orderItems: List<OrderItemInfo>,
    val completedAt: LocalDateTime = LocalDateTime.now()
) {
    data class OrderItemInfo(
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val pricePerItem: Int
    )
}