package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import java.time.LocalDateTime

data class UserCouponDetail(
    val id: Long,
    val userId: Long,
    val couponId: Long,
    val couponName: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val status: CouponStatus,
    val expiresAt: LocalDateTime,
    val issuedAt: LocalDateTime,
    val usedAt: LocalDateTime?,
)
