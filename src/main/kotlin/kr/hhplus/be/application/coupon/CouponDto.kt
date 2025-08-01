package kr.hhplus.be.application.coupon

import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.user.UserCoupon
import java.time.LocalDateTime

class CouponDto {

    data class CouponInfo(
        val couponId: Long,
        val name: String,
        val discountType: DiscountType,
        val discountValue: Int,
        val expiresAt: LocalDateTime,
        val totalQuantity: Int,
        val issuedQuantity: Int,
    )

    data class UserCouponInfo(
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
    ) {
        companion object {
            fun from(userCoupon: UserCoupon, coupon: Coupon): UserCouponInfo {
                return UserCouponInfo(
                    id = userCoupon.id,
                    userId = userCoupon.userId,
                    couponId = userCoupon.couponId,
                    couponName = coupon.name,
                    discountType = coupon.discountType,
                    discountValue = coupon.discountValue,
                    status = userCoupon.status,
                    expiresAt = coupon.expiresAt,
                    issuedAt = userCoupon.issuedAt,
                    usedAt = userCoupon.usedAt
                )
            }
        }
    }

    data class ValidatedUserCoupon(
        val userCoupon: UserCoupon,
        val coupon: Coupon
    ) {
        fun isAvailable(): Boolean = userCoupon.isAvailable() && coupon.isAvailable()
        fun isExpired(): Boolean = coupon.isExpired()
    }
}
