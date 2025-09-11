package kr.hhplus.be.application.service

import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.domain.CouponIssueEvent
import kr.hhplus.be.domain.coupon.CouponIssueEventRequestKafkaPublisher
import kr.hhplus.be.domain.coupon.CouponIssueResult
import kr.hhplus.be.domain.coupon.CouponRedisRepository
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.UserCoupon
import kr.hhplus.be.domain.user.UserCouponDetail
import kr.hhplus.be.domain.user.UserCouponRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class CouponService(
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository,
    private val couponRedisRepository: CouponRedisRepository,
    private val couponIssuePublisher: CouponIssueEventRequestKafkaPublisher
) {
    fun issue(command: CouponIssueCommand) {
        val status = couponRedisRepository.issueRequest(command.userId, command.couponId)

        when (status) {
            CouponIssueResult.ALREADY_ISSUED.value -> throw BusinessException(ErrorCode.COUPON_ALREADY_ISSUED)
            CouponIssueResult.SOLD_OUT.value -> throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
            CouponIssueResult.SUCCESS.value -> couponIssuePublisher.publish(
                CouponIssueEvent(
                    command.userId,
                    command.couponId,
                )
            )

            else -> throw BusinessException(ErrorCode.UNKNOWN_ERROR)
        }
    }

    @Transactional
    fun use(userId: Long, userCouponId: Long): UserCoupon {
        val userCoupon = getUserCouponWithValid(userId, userCouponId)

        userCoupon.use()

        return userCouponRepository.save(userCoupon)
    }

    @Transactional
    fun restore(userId: Long, userCouponId: Long): UserCoupon {
        val userCoupon =
            userCouponRepository.findById(userId) ?: throw BusinessException(ErrorCode.USER_COUPON_NOT_FOUND)
        userCoupon.restore()

        return userCouponRepository.save(userCoupon)
    }

    @Transactional(readOnly = true)
    fun calculateDiscount(userId: Long, couponId: Long, originalAmount: Int): Int {
        val coupon = couponRepository.findByIdOrThrow(couponId)
        return coupon.calculateDiscount(originalAmount)
    }

    @Transactional(readOnly = true)
    fun getUserCoupons(userId: Long): List<UserCouponDetail> {
        return userCouponRepository.findUserCouponDetails(userId)
    }

    @Transactional
    fun useCouponForSaga(userId: Long, couponId: Long) {
        use(userId, couponId)
    }

    @Transactional
    fun restoreCouponForSaga(userId: Long, couponId: Long) {
        restore(userId, couponId)
    }

    @Transactional(readOnly = true)
    fun getUserCouponWithValid(userId: Long, userCouponId: Long): UserCoupon {
        val userCoupon =
            userCouponRepository.findById(userCouponId) ?: throw BusinessException(ErrorCode.USER_COUPON_NOT_FOUND)
        val coupon =
            couponRepository.findById(userCoupon.couponId) ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)

        if (!coupon.valid()) {
            throw BusinessException(ErrorCode.COUPON_EXPIRED)
        }

        if (!userCoupon.isAvailable()) {
            throw BusinessException(ErrorCode.COUPON_NOT_AVAILABLE)
        }

        return userCoupon
    }

    @Transactional
    fun handleCouponIssueRequest(userId: Long, couponId: Long, issuedAt: LocalDateTime) {
        val coupon = couponRepository.findByIdOrThrow(couponId)

        if (!coupon.canBeIssued()) {
            throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
        }

        if (userCouponRepository.existsByUserIdAndCouponId(userId, couponId)) {
            throw BusinessException(ErrorCode.COUPON_ALREADY_ISSUED)
        }

        val userCoupon = UserCoupon.create(
            userId = userId,
            couponId = couponId,
            issuedAt = issuedAt,
        )

        val updatedCoupon = coupon.issue()
        couponRepository.save(updatedCoupon)
        userCouponRepository.save(userCoupon)
    }
}
