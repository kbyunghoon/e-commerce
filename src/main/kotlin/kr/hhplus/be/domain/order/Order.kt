package kr.hhplus.be.domain.order

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.infrastructure.entity.OrderEntity
import java.time.LocalDateTime

data class Order(
    val id: Long = 0,
    val userId: Long,
    val userCouponId: Long?,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    var status: OrderStatus = OrderStatus.PENDING,
    val orderDate: LocalDateTime?,
    val expireDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun toEntity(): OrderEntity {
        return OrderEntity(
            id = this.id,
            userId = this.userId,
            userCouponId = this.userCouponId,
            originalAmount = this.originalAmount,
            discountAmount = this.discountAmount,
            finalAmount = this.finalAmount,
            status = this.status,
            orderDate = this.orderDate,
            expiresAt = this.expireDate,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }

    companion object {
        const val MIN_ORDER_AMOUNT = 1
        const val MAX_ORDER_AMOUNT = 1_000_000
        const val MAX_DISCOUNT_PERCENTAGE = 100

        fun create(
            userId: Long,
            originalAmount: Int,
            discountAmount: Int,
            finalAmount: Int,
            userCouponId: Long?,
            orderedAt: LocalDateTime = LocalDateTime.now()
        ): Order {
            validateBusinessRules(userId, originalAmount, discountAmount, finalAmount)

            return Order(
                id = 0,
                userId = userId,
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = userCouponId,
                expireDate = LocalDateTime.now().plusMinutes(30),
                orderDate = null,
                createdAt = LocalDateTime.now(),
            )
        }

        private fun validateBusinessRules(
            userId: Long,
            originalAmount: Int,
            discountAmount: Int,
            finalAmount: Int
        ) {
            if (userId <= 0) {
                throw BusinessException(ErrorCode.INVALID_USER_ID)
            }

            if (originalAmount !in MIN_ORDER_AMOUNT..MAX_ORDER_AMOUNT) {
                throw BusinessException(ErrorCode.INVALID_ORDER_AMOUNT)
            }

            if (discountAmount < 0) {
                throw BusinessException(ErrorCode.INVALID_DISCOUNT_AMOUNT)
            }

            if (discountAmount > originalAmount) {
                throw BusinessException(ErrorCode.DISCOUNT_EXCEEDS_ORDER_AMOUNT)
            }

            if (finalAmount < 0) {
                throw BusinessException(ErrorCode.INVALID_FINAL_AMOUNT)
            }

            if (finalAmount != originalAmount - discountAmount) {
                throw BusinessException(ErrorCode.INVALID_AMOUNT_CALCULATION)
            }

            val discountPercentage = if (originalAmount > 0) (discountAmount * 100) / originalAmount else 0
            if (discountPercentage > MAX_DISCOUNT_PERCENTAGE) {
                throw BusinessException(ErrorCode.DISCOUNT_RATE_EXCEEDED)
            }
        }
    }

    fun completeOrder() {
        validateInvariants()

        if (!isPending()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)
        }
        this.status = OrderStatus.COMPLETED

        if (this.status != OrderStatus.COMPLETED) {
            throw BusinessException(ErrorCode.ORDER_STATE_CHANGE_FAILED)
        }
    }

    fun cancelOrder() {
        validateInvariants()

        if (isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }
        this.status = OrderStatus.CANCELLED

        if (this.status != OrderStatus.CANCELLED) {
            throw BusinessException(ErrorCode.ORDER_STATE_CHANGE_FAILED)
        }
    }

    fun isPending(): Boolean = status == OrderStatus.PENDING

    fun isCompleted(): Boolean = status == OrderStatus.COMPLETED

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED

    fun canBeCompleted(): Boolean = isPending()

    fun canBeCancelled(): Boolean = !isCancelled()

    private fun validateInvariants() {
        if (originalAmount < MIN_ORDER_AMOUNT) {
            throw BusinessException(ErrorCode.INVALID_ORDER_AMOUNT)
        }
        if (discountAmount < 0) {
            throw BusinessException(ErrorCode.INVALID_DISCOUNT_AMOUNT)
        }
        if (discountAmount > originalAmount) {
            throw BusinessException(ErrorCode.DISCOUNT_EXCEEDS_ORDER_AMOUNT)
        }
        if (finalAmount < 0) {
            throw BusinessException(ErrorCode.INVALID_FINAL_AMOUNT)
        }
        if (finalAmount != originalAmount - discountAmount) {
            throw BusinessException(ErrorCode.INVALID_AMOUNT_CALCULATION)
        }
    }
}

data class OrderItem(
    val orderId: Long? = null,
    val productId: Long,
    val quantity: Int,
    val pricePerItem: Int,
    var status: OrderStatus = OrderStatus.PENDING,
) {
    companion object {
        const val MIN_QUANTITY = 1
        const val MAX_QUANTITY = 100
        const val MIN_PRICE = 1
        const val MAX_PRICE = 1_000_000

        fun create(
            productId: Long,
            quantity: Int,
            pricePerItem: Int,
            orderId: Long? = null
        ): OrderItem {
            validateBusinessRules(productId, quantity, pricePerItem)

            return OrderItem(
                orderId = orderId,
                productId = productId,
                quantity = quantity,
                pricePerItem = pricePerItem,
                status = OrderStatus.PENDING
            )
        }

        private fun validateBusinessRules(
            productId: Long,
            quantity: Int,
            pricePerItem: Int
        ) {
            println(productId)
            println(quantity)
            println(pricePerItem)
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

    fun completeOrder() {
        validateInvariants()

        if (!isPending()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)
        }
        this.status = OrderStatus.COMPLETED

        if (this.status != OrderStatus.COMPLETED) {
            throw BusinessException(ErrorCode.ORDER_STATE_CHANGE_FAILED)
        }
    }

    fun cancelOrder() {
        validateInvariants()

        if (isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }
        this.status = OrderStatus.CANCELLED

        if (this.status != OrderStatus.CANCELLED) {
            throw BusinessException(ErrorCode.ORDER_STATE_CHANGE_FAILED)
        }
    }

    fun isPending(): Boolean = status == OrderStatus.PENDING

    fun isCompleted(): Boolean = status == OrderStatus.COMPLETED

    fun isCancelled(): Boolean = status == OrderStatus.CANCELLED

    fun canBeCompleted(): Boolean = isPending()

    fun canBeCancelled(): Boolean = !isCancelled()

    fun getTotalPrice(): Int = quantity * pricePerItem

    private fun validateInvariants() {
        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw BusinessException(ErrorCode.INVALID_ORDER_QUANTITY)
        }
        if (pricePerItem < MIN_PRICE || pricePerItem > MAX_PRICE) {
            throw BusinessException(ErrorCode.INVALID_PRODUCT_PRICE)
        }
        if (productId <= 0) {
            throw BusinessException(ErrorCode.INVALID_PRODUCT_ID)
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
