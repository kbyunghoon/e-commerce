package kr.hhplus.be.application.order

import kr.hhplus.be.domain.order.OrderItem

data class PaymentOperationsStatus(
    var balanceDeducted: Boolean = false,
    val deductedProducts: MutableList<OrderItem> = mutableListOf(),
    var couponUsed: Boolean = false
)
