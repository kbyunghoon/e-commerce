package kr.hhplus.be.application.service

import kr.hhplus.be.application.coupon.CouponDto
import kr.hhplus.be.application.coupon.CouponDto.UserCouponInfo
import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.domain.coupon.CouponIssueResult
import kr.hhplus.be.domain.coupon.CouponRedisRepository
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.UserCoupon
import kr.hhplus.be.domain.user.UserCouponRepository
import kr.hhplus.be.global.lock.CouponLockKeyProvider
import kr.hhplus.be.global.lock.DistributedLock
import kr.hhplus.be.global.lock.LockResource
import kr.hhplus.be.global.lock.LockStrategy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val couponRedisRepository: CouponRedisRepository,
) {
    fun issue(command: CouponIssueCommand) {
        val status = couponRedisRepository.issueRequest(command.userId, command.couponId)

        when (status) {
            CouponIssueResult.ALREADY_ISSUED.value -> throw BusinessException(ErrorCode.COUPON_ALREADY_ISSUED)
            CouponIssueResult.SOLD_OUT.value -> throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
            CouponIssueResult.SUCCESS.value -> {}
            else -> throw BusinessException(ErrorCode.UNKNOWN_ERROR)
        }
    }

    @Transactional
    fun use(userId: Long, couponId: Long): UserCouponInfo {
        val validated = findAndValidateUserCoupon(userId, couponId)

        validated.userCoupon.use()
        val updatedUserCoupon = userCouponRepository.save(validated.userCoupon)

        return UserCouponInfo.from(updatedUserCoupon, validated.coupon)
    }

    @Transactional
    fun restore(userId: Long, couponId: Long): UserCouponInfo {
        val validated = findAndValidateUserCoupon(userId, couponId)

        validated.userCoupon.restore()
        val updatedUserCoupon = userCouponRepository.save(validated.userCoupon)

        return UserCouponInfo.from(updatedUserCoupon, validated.coupon)
    }

    @Transactional(readOnly = true)
    fun calculateDiscount(userId: Long, couponId: Long, originalAmount: Int): Int {
        val coupon = couponRepository.findByIdOrThrow(couponId)
        return coupon.calculateDiscount(originalAmount)
    }

    @Transactional(readOnly = true)
    fun getUserCoupons(userId: Long): List<UserCouponInfo> {
        val userCoupons = userCouponRepository.findByUserId(userId)

        return userCoupons.map { userCoupon ->
            val coupon = couponRepository.findByIdOrThrow(userCoupon.couponId)

            UserCouponInfo.from(userCoupon, coupon)
        }
    }

    fun findAndValidateUserCoupon(userId: Long, couponId: Long): CouponDto.ValidatedUserCoupon {
        val userCoupon = userCouponRepository.findByUserIdAndCouponId(userId, couponId)
            ?: throw BusinessException(ErrorCode.USER_COUPON_NOT_FOUND)

        val coupon = couponRepository.findByIdOrThrow(userCoupon.couponId)

        if (coupon.isExpired()) {
            userCoupon.expire()
            userCouponRepository.save(userCoupon)
            throw BusinessException(ErrorCode.COUPON_EXPIRED)
        }

        return CouponDto.ValidatedUserCoupon(userCoupon, coupon)
    }
}
