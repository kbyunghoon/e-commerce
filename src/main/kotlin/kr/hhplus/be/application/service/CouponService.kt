package kr.hhplus.be.application.service

import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.application.coupon.CouponDto.UserCouponInfo
import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.UserCoupon
import kr.hhplus.be.domain.user.UserCouponRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) {

    fun issue(command: CouponIssueCommand): UserCouponInfo {
        val coupon = couponRepository.findById(command.couponId)
            ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)

        if (coupon.expiresAt.isBefore(LocalDateTime.now())) {
            throw BusinessException(ErrorCode.COUPON_EXPIRED)
        }

        if (userCouponRepository.existsByUserIdAndCouponId(command.userId, command.couponId)) {
            throw BusinessException(ErrorCode.COUPON_ALREADY_ISSUED)
        }

        coupon.issue()
        couponRepository.save(coupon)

        val userCoupon = UserCoupon(
            userId = command.userId,
            couponId = command.couponId,
            status = CouponStatus.AVAILABLE
        )
        val savedUserCoupon = userCouponRepository.save(userCoupon)

        return UserCouponInfo.from(savedUserCoupon, coupon)
    }

    fun use(userId: Long, couponId: Long): UserCouponInfo {
        val (userCoupon, coupon) = findAndValidateUserCoupon(userId, couponId)

        userCoupon.use()
        val updatedUserCoupon = userCouponRepository.save(userCoupon)

        return UserCouponInfo.from(updatedUserCoupon, coupon)
    }

    fun restore(userId: Long, couponId: Long): UserCouponInfo {
        val (userCoupon, coupon) = findAndValidateUserCoupon(userId, couponId)

        userCoupon.restore()
        val updatedUserCoupon = userCouponRepository.save(userCoupon)

        return UserCouponInfo.from(updatedUserCoupon, coupon)
    }

    fun calculateDiscount(userId: Long, couponId: Long, originalAmount: Int): Int {
        val coupon = couponRepository.findById(couponId) ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)

        val discount = when (coupon.discountType) {
            DiscountType.PERCENTAGE -> originalAmount * coupon.discountValue / 100
            DiscountType.FIXED -> coupon.discountValue
        }

        return discount.coerceAtMost(originalAmount)
    }

    fun getUserCoupons(userId: Long): List<UserCouponInfo> {
        val userCoupons = userCouponRepository.findByUserId(userId)

        return userCoupons.map { userCoupon ->
            val coupon = couponRepository.findById(userCoupon.couponId)
                ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)

            UserCouponInfo.from(userCoupon, coupon)
        }
    }

    fun findAndValidateUserCoupon(userId: Long, couponId: Long): Pair<UserCoupon, Coupon> {
        val userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
            ?: throw BusinessException(ErrorCode.USER_COUPON_NOT_FOUND)

        val coupon = couponRepository.findById(userCoupon.couponId)
            ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)

        if (coupon.expiresAt.isBefore(LocalDateTime.now())) {
            userCoupon.expire()
            userCouponRepository.save(userCoupon)
            throw BusinessException(ErrorCode.COUPON_EXPIRED)
        }

        return Pair(userCoupon, coupon)
    }
}
