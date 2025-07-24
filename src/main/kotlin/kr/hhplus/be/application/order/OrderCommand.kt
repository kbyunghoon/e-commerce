package kr.hhplus.be.application.order

import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.presentation.dto.request.OrderRequest
import kr.hhplus.be.presentation.dto.request.PaymentRequest

data class OrderItemCreateCommand(
    val productId: Long,
    val quantity: Int
)

data class OrderCreateCommand(
    val userId: Long,
    val items: List<OrderItemCreateCommand>,
    val couponId: Long?,
) {
    companion object {
        fun of(request: OrderRequest): OrderCreateCommand {
            return OrderCreateCommand(
                userId = request.userId,
                items = request.items,
                couponId = request.couponId
            )
        }
    }

    fun toBalanceDeductCommand(finalAmount: Int): BalanceDeductCommand {
        return BalanceDeductCommand(
            userId = this.userId,
            amount = finalAmount
        )
    }
}

data class PaymentProcessCommand(
    val orderId: Long,
    val userId: Long,
) {
    companion object {
        fun of(request: PaymentRequest): PaymentProcessCommand {
            return PaymentProcessCommand(
                orderId = request.orderId,
                userId = request.userId
            )
        }
    }
}
