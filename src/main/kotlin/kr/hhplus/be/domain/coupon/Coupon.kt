package kr.hhplus.be.domain.coupon

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.infrastructure.entity.CouponEntity
import java.time.LocalDateTime
import kotlin.math.min

data class Coupon(
    val id: Long = 0,
    val name: String,
    val code: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val expiresAt: LocalDateTime,
    val totalQuantity: Int,
    var issuedQuantity: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun issue() {
        if (!canBeIssued()) {
            throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
        }
        this.issuedQuantity++
        this.updatedAt = LocalDateTime.now()
    }

    fun restore() {
        if (issuedQuantity <= 0) {
            throw BusinessException(ErrorCode.COUPON_NOT_FOUND)
        }
        this.issuedQuantity--
        this.updatedAt = LocalDateTime.now()
    }

    fun calculateDiscount(amount: Int): Int {
        val discount = when (discountType) {
            DiscountType.PERCENTAGE -> amount * discountValue / 100
            DiscountType.FIXED -> discountValue
        }
        return min(amount, discount)
    }
    
    fun isAvailable(): Boolean = hasRemainingQuantity() && !isExpired()
    
    fun hasRemainingQuantity(): Boolean = issuedQuantity < totalQuantity
    
    fun isExpired(): Boolean = LocalDateTime.now().isAfter(expiresAt)
    
    fun isSoldOut(): Boolean = issuedQuantity >= totalQuantity
    
    fun canBeIssued(): Boolean = hasRemainingQuantity() && !isExpired()
    
    fun getRemainingQuantity(): Int = totalQuantity - issuedQuantity

    fun toEntity(): CouponEntity {
        return CouponEntity(
            id = this.id,
            name = this.name,
            code = this.code,
            discountType = this.discountType,
            discountValue = this.discountValue,
            expiresAt = this.expiresAt,
            totalQuantity = this.totalQuantity,
            issuedQuantity = this.issuedQuantity,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}
