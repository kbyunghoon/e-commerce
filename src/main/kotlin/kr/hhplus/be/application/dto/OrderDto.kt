package kr.hhplus.be.application.dto

data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

data class CreateOrderRequest(
    val userId: Long,
    val items: List<OrderItemRequest>,
    val couponId: Long?
)

data class PaymentRequest(
    val orderId: String,
    val userId: Long,
)
