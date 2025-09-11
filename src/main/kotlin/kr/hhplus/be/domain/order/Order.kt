package kr.hhplus.be.domain.order

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class Order(
    val id: Long? = null,
    val userId: Long,
    val orderNumber: String,
    val userCouponId: Long?,
    val originalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val status: OrderStatus = OrderStatus.PENDING,
    val orderItems: List<OrderItem>?,
    val orderDate: LocalDateTime?,
    val expireDate: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {

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
                userId = userId,
                orderNumber = generateOrderNumber(orderedAt),
                originalAmount = originalAmount,
                discountAmount = discountAmount,
                finalAmount = finalAmount,
                status = OrderStatus.PENDING,
                userCouponId = userCouponId,
                expireDate = orderedAt.plusMinutes(30),
                orderDate = null,
                orderItems = null,
                createdAt = orderedAt,
            )
        }

        private fun generateOrderNumber(dateTime: LocalDateTime = LocalDateTime.now()): String {
            val formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss")
            val dateTimePart = dateTime.format(formatter)
            val randomPart = String.format("%02d", Random.nextInt(0, 100))
            return "T$dateTimePart$randomPart"
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

    fun withOrderItems(orderItems: List<OrderItem>): Order {
        return this.copy(orderItems = orderItems)
    }

    fun completeOrder(): Order {
        validateInvariants()

        if (isCompleted()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_COMPLETED)
        }

        if (isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }

        return this.copy(status = OrderStatus.COMPLETED, orderDate = LocalDateTime.now())
    }

    fun cancelOrder(): Order {
        validateInvariants()

        if (isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }

        return this.copy(status = OrderStatus.CANCELLED, orderDate = LocalDateTime.now())
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