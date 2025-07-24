package kr.hhplus.be.application.order

import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import kr.hhplus.be.application.product.ProductDto
import java.time.LocalDateTime

class OrderDto {

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

    data class CalculatedOrderDetails(
        val totalAmount: Int,
        val discountAmount: Int,
        val finalAmount: Int,
        val products: List<ProductDto.ProductInfo>
    )

    data class OrderCreateDto(
        val userId: Long,
        val items: List<OrderItem>,
        val originalAmount: Int,
        val discountAmount: Int,
        val finalAmount: Int,
        val couponId: Long? = null
    )
}
