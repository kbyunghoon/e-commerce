package kr.hhplus.be.application.facade

import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.application.coupon.UserCouponInfo
import kr.hhplus.be.application.service.CouponService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CouponFacade(
    private val couponService: CouponService
) {

    @Transactional
    fun issueCoupon(command: CouponIssueCommand): UserCouponInfo {
        return couponService.issue(command)
    }

    @Transactional(readOnly = true)
    fun getUserCoupons(userId: Long): List<UserCouponInfo> {
        return couponService.getUserCoupons(userId)
    }
}
