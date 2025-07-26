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
    companion object {
        const val MIN_DISCOUNT_VALUE = 1
        const val MAX_PERCENTAGE_DISCOUNT = 100
        const val MAX_FIXED_DISCOUNT = 100_000
        const val MIN_TOTAL_QUANTITY = 1
        const val MAX_TOTAL_QUANTITY = 10_000
        const val MIN_COUPON_NAME_LENGTH = 1
        const val MAX_COUPON_NAME_LENGTH = 100
        
        fun create(
            name: String,
            code: String,
            discountType: DiscountType,
            discountValue: Int,
            expiresAt: LocalDateTime,
            totalQuantity: Int
        ): Coupon {
            validateBusinessRules(name, discountType, discountValue, expiresAt, totalQuantity)
            
            return Coupon(
                name = name,
                code = code,
                discountType = discountType,
                discountValue = discountValue,
                expiresAt = expiresAt,
                totalQuantity = totalQuantity,
                issuedQuantity = 0
            )
        }
        
        private fun validateBusinessRules(
            name: String,
            discountType: DiscountType,
            discountValue: Int,
            expiresAt: LocalDateTime,
            totalQuantity: Int
        ) {
            if (name.length !in MIN_COUPON_NAME_LENGTH..MAX_COUPON_NAME_LENGTH) {
                throw BusinessException(ErrorCode.INVALID_COUPON_NAME)
            }
            
            if (discountValue < MIN_DISCOUNT_VALUE) {
                throw BusinessException(ErrorCode.INVALID_DISCOUNT_VALUE)
            }
            
            when (discountType) {
                DiscountType.PERCENTAGE -> {
                    if (discountValue > MAX_PERCENTAGE_DISCOUNT) {
                        throw BusinessException(ErrorCode.INVALID_PERCENTAGE_DISCOUNT)
                    }
                }
                DiscountType.FIXED -> {
                    if (discountValue > MAX_FIXED_DISCOUNT) {
                        throw BusinessException(ErrorCode.INVALID_FIXED_DISCOUNT)
                    }
                }
            }
            
            if (!expiresAt.isAfter(LocalDateTime.now())) {
                throw BusinessException(ErrorCode.INVALID_EXPIRY_DATE)
            }
            
            if (totalQuantity !in MIN_TOTAL_QUANTITY..MAX_TOTAL_QUANTITY) {
                throw BusinessException(ErrorCode.INVALID_TOTAL_QUANTITY)
            }
        }
    }
    fun issue() {
        validateInvariants()
        
        if (!canBeIssued()) {
            throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
        }
        this.issuedQuantity++
        this.updatedAt = LocalDateTime.now()
        
        if (this.issuedQuantity > this.totalQuantity) {
            throw BusinessException(ErrorCode.COUPON_ISSUE_LIMIT_EXCEEDED)
        }
    }

    fun restore() {
        validateInvariants()
        
        if (issuedQuantity <= 0) {
            throw BusinessException(ErrorCode.COUPON_NOT_FOUND)
        }
        this.issuedQuantity--
        this.updatedAt = LocalDateTime.now()
        
        if (this.issuedQuantity < 0) {
            throw BusinessException(ErrorCode.INVALID_COUPON_STATE)
        }
    }

    fun calculateDiscount(amount: Int): Int {
        validateInvariants()
        
        if (amount <= 0) {
            throw BusinessException(ErrorCode.INVALID_DISCOUNT_AMOUNT)
        }
        
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
    
    private fun validateInvariants() {
        if (issuedQuantity < 0) {
            throw BusinessException(ErrorCode.INVALID_COUPON_STATE)
        }
        if (issuedQuantity > totalQuantity) {
            throw BusinessException(ErrorCode.COUPON_ISSUE_LIMIT_EXCEEDED)
        }
        if (discountValue < MIN_DISCOUNT_VALUE) {
            throw BusinessException(ErrorCode.INVALID_DISCOUNT_VALUE)
        }
        if (totalQuantity < MIN_TOTAL_QUANTITY) {
            throw BusinessException(ErrorCode.INVALID_TOTAL_QUANTITY)
        }
    }

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
