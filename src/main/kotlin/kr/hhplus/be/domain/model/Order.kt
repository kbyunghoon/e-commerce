package kr.hhplus.be.domain.model

import kr.hhplus.be.domain.enums.OrderStatus
import java.time.LocalDateTime

data class Order(
    val orderId: String,
    val userId: Long,
    val items: List<OrderItem>,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val status: OrderStatus = OrderStatus.PENDING
)

data class OrderItem(
    val productId: Long,
    val productName: String,
    val price: Int,
    val quantity: Int
)

data class Payment(
    val orderId: Long,
    val orderNumber: String,
    val userId: Long,
    val finalAmount: Int,
    val status: OrderStatus,
    val orderedAt: LocalDateTime
)
