package kr.hhplus.be.domain.order

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

data class OrderItem(
    val id: Long? = null,
    val orderId: Long? = null,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val pricePerItem: Int,
    val status: OrderStatus = OrderStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100
        const val MIN_PRICE = 1
        const val MAX_PRICE = 100_000_000

        fun create(
            productId: Long,
            quantity: Int,
            productName: String,
            pricePerItem: Int,
            orderId: Long
        ): OrderItem {
            validateBusinessRules(productId, quantity, pricePerItem)

            return OrderItem(
                orderId = orderId,
                productId = productId,
                productName = productName,
                quantity = quantity,
                pricePerItem = pricePerItem
            )
        }

        private fun validateBusinessRules(
            productId: Long,
            quantity: Int,
            pricePerItem: Int
        ) {
            if (productId <= 0) {
                throw BusinessException(ErrorCode.INVALID_PRODUCT_ID)
            }

            if (quantity !in MIN_QUANTITY..MAX_QUANTITY) {
                throw BusinessException(ErrorCode.INVALID_ORDER_QUANTITY)
            }

            if (pricePerItem !in MIN_PRICE..MAX_PRICE) {
                throw BusinessException(ErrorCode.INVALID_PRODUCT_PRICE)
            }
        }
    }

    fun completeOrder(): OrderItem {
        validateInvariants()

        if (isCompleted()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_COMPLETED)
        }

        if (isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }

        return this.copy(status = OrderStatus.COMPLETED, updatedAt = LocalDateTime.now())
    }

    fun cancelOrder(): OrderItem {
        validateInvariants()

        if (isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }

        return this.copy(status = OrderStatus.CANCELLED, updatedAt = LocalDateTime.now())
    }

    fun isPending(): Boolean = status == OrderStatus.PENDING

    fun isCompleted(): Boolean = status == OrderStatus.COMPLETED

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED

    fun canBeCompleted(): Boolean = isPending()

    fun canBeCancelled(): Boolean = !isCancelled()

    fun getTotalPrice(): Int = quantity * pricePerItem

    private fun validateInvariants() {
        if (quantity !in MIN_QUANTITY..MAX_QUANTITY) {
            throw BusinessException(ErrorCode.INVALID_ORDER_QUANTITY)
        }
        if (pricePerItem !in MIN_PRICE..MAX_PRICE) {
            throw BusinessException(ErrorCode.INVALID_PRODUCT_PRICE)
        }
        if (productId <= 0) {
            throw BusinessException(ErrorCode.INVALID_PRODUCT_ID)
        }
    }
}
