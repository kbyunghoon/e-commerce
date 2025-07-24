package kr.hhplus.be.application.order

import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

class OrderDto {

    data class OrderInfo(
        val id: Long? = null,
        val userId: Long,
        val userCouponId: Long? = null,
        val originalAmount: Int,
        val discountAmount: Int,
        val finalAmount: Int,
        val orderedAt: LocalDateTime,
        val orderItems: List<OrderItemInfo>,
        val status: OrderStatus
    ) {
        companion object {
            fun from(order: Order, items: List<OrderItem>): OrderInfo {
                return OrderInfo(
                    id = order.id,
                    userId = order.userId,
                    userCouponId = order.userCouponId,
                    originalAmount = order.originalAmount,
                    discountAmount = order.discountAmount,
                    finalAmount = order.finalAmount,
                    orderedAt = order.orderedAt,
                    orderItems = items.map { OrderItemInfo.from(it) },
                    status = order.status
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
                    price = orderItem.pricePerItem
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
