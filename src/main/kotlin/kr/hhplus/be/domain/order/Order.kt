package kr.hhplus.be.domain.order

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.infrastructure.entity.OrderEntity
import java.time.LocalDateTime

data class Order(
    val id: Long? = null,
    val userId: Long,
    val userCouponId: Long?,
    val items: List<OrderItem>,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    var status: OrderStatus = OrderStatus.PENDING,
    val orderedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            userId: Long,
            items: List<OrderItem>,
            originalAmount: Int,
            discountAmount: Int,
            finalAmount: Int,
            userCouponId: Long?,
            orderedAt: LocalDateTime = LocalDateTime.now()
        ): Order {
            return Order(
                id = null,
                userId = userId,
                items = items,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = userCouponId,
                orderedAt = orderedAt
            )
        }
    }

    fun completeOrder() {
        if (this.status != OrderStatus.PENDING) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)
        }
        this.status = OrderStatus.COMPLETED
    }

    fun cancelOrder() {
        if (this.status == OrderStatus.CANCELLED) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }
        this.status = OrderStatus.CANCELLED
    }


    fun toEntity(): OrderEntity {
        return OrderEntity(
            orderId = this.id ?: 0,
            userId = this.userId,
            userCouponId = this.userCouponId,
            originalAmount = this.originalAmount,
            discountAmount = this.discountAmount,
            finalAmount = this.finalAmount,
            status = this.status,
            orderDate = this.orderedAt,
            expiresAt = LocalDateTime.now().plusMinutes(10)
        )
    }
}

data class OrderItem(
    val productId: Long,
    val productName: String,
    val price: Int,
    val quantity: Int
) {
    companion object {
        fun from(product: Product, quantity: Int): OrderItem {
            return OrderItem(
                productId = product.id,
                productName = product.name,
                price = product.price,
                quantity = quantity
            )
        }
    }
}

data class Payment(
    val orderId: Long,
    val orderNumber: String,
    val userId: Long,
    val finalAmount: Int,
    val status: OrderStatus,
    val orderedAt: LocalDateTime
)
