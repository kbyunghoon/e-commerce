package kr.hhplus.be.application.order

import kr.hhplus.be.domain.order.OrderItem

data class OrderCreateDto(
    val userId: Long,
    val items: List<OrderItem>,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val couponId: Long? = null
)
