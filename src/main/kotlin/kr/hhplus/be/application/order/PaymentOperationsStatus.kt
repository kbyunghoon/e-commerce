package kr.hhplus.be.application.order

data class PaymentOperationsStatus(
    var balanceDeducted: Boolean = false,
    val deductedProducts: MutableList<OrderDto.OrderItemDetails> = mutableListOf(),
    var couponUsed: Boolean = false
)
