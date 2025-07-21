package kr.hhplus.be.application.dto

import kr.hhplus.be.adapter.`in`.web.dto.request.OrderItemRequest
import kr.hhplus.be.adapter.`in`.web.dto.request.OrderRequest
import kr.hhplus.be.adapter.`in`.web.dto.request.PaymentRequest
import kr.hhplus.be.domain.enums.OrderStatus
import java.time.LocalDateTime

data class OrderItemCreateCommand(
    val productId: Long,
    val quantity: Int
) {
    companion object {
        fun of(request: OrderItemRequest): OrderItemCreateCommand {
            return OrderItemCreateCommand(
                productId = request.productId,
                quantity = request.quantity
            )
        }
    }
}

data class OrderCreateCommand(
    val userId: Long,
    val items: List<OrderItemCreateCommand>,
    val couponId: Long?,
) {
    companion object {
        fun of(request: OrderRequest): OrderCreateCommand {
            return OrderCreateCommand(
                userId = request.userId,
                items = request.items.map { OrderItemCreateCommand.of(it) },
                couponId = request.couponId
            )
        }
    }
}

data class PaymentProcessCommand(
    val orderId: Long,
    val userId: Long,
) {
    companion object {
        fun of(orderId: Long, request: PaymentRequest): PaymentProcessCommand {
            return PaymentProcessCommand(
                orderId = orderId,
                userId = request.userId
            )
        }
    }
}

data class OrderInfo(
    val id: Long,
    val userId: Long,
    val orderStatus: OrderStatus,
    val totalAmount: Int,
    val orderedAt: LocalDateTime,
    val orderItems: List<OrderItemInfo>,
)

data class OrderItemInfo(
    val productId: Long,
    val quantity: Int,
    val price: Int,
)