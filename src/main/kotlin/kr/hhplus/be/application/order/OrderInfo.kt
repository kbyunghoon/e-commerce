package kr.hhplus.be.application.order

import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

data class OrderInfo(
    val id: Long?,
    val userId: Long,
    val orderStatus: OrderStatus,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val orderedAt: LocalDateTime,
    val orderItems: List<OrderItemInfo>
) {
    companion object {
        fun from(order: Order): OrderInfo {
            return OrderInfo(
                id = order.id,
                userId = order.userId,
                orderStatus = order.status,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                finalAmount = order.finalAmount,
                orderedAt = order.orderedAt,
                orderItems = order.items.map { OrderItemInfo.from(it) }
            )
        }
    }
}
