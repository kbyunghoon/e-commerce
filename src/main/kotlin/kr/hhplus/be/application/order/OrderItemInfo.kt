package kr.hhplus.be.application.order

import kr.hhplus.be.domain.order.OrderItem

data class OrderItemInfo(
    val productId: Long,
    val quantity: Int,
    val price: Int
) {
    companion object {
        fun from(orderItem: OrderItem): OrderItemInfo {
            return OrderItemInfo(
                productId = orderItem.productId,
                quantity = orderItem.quantity,
                price = orderItem.price
            )
        }
    }
}
