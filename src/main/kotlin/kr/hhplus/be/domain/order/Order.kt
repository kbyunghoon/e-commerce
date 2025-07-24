package kr.hhplus.be.domain.order

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

data class Order(
    val id: Long? = null,
    val userId: Long,
    val userCouponId: Long?,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    var status: OrderStatus = OrderStatus.PENDING,
    val orderedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun create(
            userId: Long,
            originalAmount: Int,
            discountAmount: Int,
            finalAmount: Int,
            userCouponId: Long?,
            orderedAt: LocalDateTime = LocalDateTime.now()
        ): Order {
            return Order(
                id = null,
                userId = userId,
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
}

data class OrderItem(
    val orderId: Long? = null,
    val productId: Long,
    val quantity: Int,
    val pricePerItem: Int,
    var status: OrderStatus = OrderStatus.PENDING,
) {
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
}

data class Payment(
    val orderId: Long,
    val orderNumber: String,
    val userId: Long,
    val finalAmount: Int,
    val status: OrderStatus,
    val orderedAt: LocalDateTime
)
