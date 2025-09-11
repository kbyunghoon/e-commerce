package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

data class UserCoupon(
    val id: Long? = null,
    val userId: Long,
    val couponId: Long,
    var status: CouponStatus = CouponStatus.AVAILABLE,
    val issuedAt: LocalDateTime = LocalDateTime.now(),
    var usedAt: LocalDateTime? = null,
) {
    companion object {
        fun create(userId:Long, couponId:Long, issuedAt: LocalDateTime): UserCoupon {
            return UserCoupon(
                userId = userId,
                couponId = couponId,
                issuedAt = issuedAt,
            )
        }
    }

    fun use() {
        if (!isAvailable()) {
            throw BusinessException(ErrorCode.COUPON_NOT_AVAILABLE)
        }
        this.status = CouponStatus.USED
        this.usedAt = LocalDateTime.now()
    }

    fun restore() {
        if (!isUsed()) {
            throw BusinessException(ErrorCode.COUPON_NOT_USED)
        }
        this.status = CouponStatus.AVAILABLE
        this.usedAt = null
    }

    fun isAvailable(): Boolean = status == CouponStatus.AVAILABLE

    fun isUsed(): Boolean = status == CouponStatus.USED

    fun isExpired(): Boolean = status == CouponStatus.EXPIRED

    fun expire() {
        this.status = CouponStatus.EXPIRED
    }
}
