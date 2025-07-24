package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.infrastructure.entity.UserCouponEntity
import java.time.LocalDateTime

data class UserCoupon(
    val id: Long = 0,
    val userId: Long,
    val couponId: Long,
    var status: CouponStatus,
    val issuedAt: LocalDateTime = LocalDateTime.now(),
    var usedAt: LocalDateTime? = null,
    val coupon: Coupon? = null
) {
    fun use() {
        if (status != CouponStatus.AVAILABLE) {
            throw BusinessException(ErrorCode.COUPON_NOT_AVAILABLE)
        }
        this.status = CouponStatus.USED
        this.usedAt = LocalDateTime.now()
    }

    fun restore() {
        if (status != CouponStatus.USED) {
            throw BusinessException(ErrorCode.COUPON_NOT_USED)
        }
        this.status = CouponStatus.AVAILABLE
        this.usedAt = null
    }

    fun expire() {
        this.status = CouponStatus.EXPIRED
    }

    fun toEntity(): UserCouponEntity {
        return UserCouponEntity(
            id = this.id,
            userId = this.userId,
            couponId = this.couponId,
            status = this.status,
            issuedAt = this.issuedAt,
            usedAt = this.usedAt
        )
    }
}
