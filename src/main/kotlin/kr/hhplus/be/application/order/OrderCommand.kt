package kr.hhplus.be.application.order

data class OrderItemCreateCommand(
    val productId: Long,
    val quantity: Int
)

data class OrderCreateCommand(
    val userId: Long,
    val items: List<OrderItemCreateCommand>,
    val userCouponId: Long?,
)

data class PaymentProcessCommand(
    val orderId: Long,
    val userId: Long,
)