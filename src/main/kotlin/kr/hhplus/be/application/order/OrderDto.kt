package kr.hhplus.be.application.order

import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

class OrderDto {

    data class OrderDetails(
        val id: Long? = null,
        val userId: Long,
        val orderNumber: String,
        val userCouponId: Long? = null,
        val originalAmount: Int,
        val discountAmount: Int,
        val finalAmount: Int,
        val orderDate: LocalDateTime?,
        val orderItems: List<OrderItemDetails>,
        val status: OrderStatus
    ) {
        companion object {
            fun from(order: Order, items: List<OrderItem>): OrderDetails {
                return OrderDetails(
                    id = order.id,
                    userId = order.userId,
                    orderNumber = order.orderNumber,
                    userCouponId = order.userCouponId,
                    originalAmount = order.originalAmount,
                    discountAmount = order.discountAmount,
                    finalAmount = order.finalAmount,
                    orderDate = order.orderDate,
                    orderItems = items.map { OrderItemDetails.from(it) },
                    status = order.status
                )
            }
        }
    }

    data class OrderItemDetails(
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val price: Int
    ) {
        companion object {
            fun from(orderItem: OrderItem): OrderItemDetails {
                return OrderItemDetails(
                    productId = orderItem.productId,
                    productName = orderItem.productName,
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
