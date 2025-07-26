package kr.hhplus.be.application.order

import kr.hhplus.be.application.balance.BalanceDeductCommand

data class OrderItemCreateCommand(
    val productId: Long,
    val quantity: Int
)

data class OrderCreateCommand(
    val userId: Long,
    val items: List<OrderItemCreateCommand>,
    val couponId: Long?,
) {
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
)