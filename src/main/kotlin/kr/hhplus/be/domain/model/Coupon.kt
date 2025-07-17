package kr.hhplus.be.domain.model

import kr.hhplus.be.domain.enums.CouponStatus
import kr.hhplus.be.domain.enums.DiscountType
import java.time.LocalDateTime

data class Coupon(
    val userCouponId: Long,
    val couponId: Long,
    val couponName: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val status: CouponStatus,
    val expiryDate: LocalDateTime,
    val issuedAt: LocalDateTime
)
