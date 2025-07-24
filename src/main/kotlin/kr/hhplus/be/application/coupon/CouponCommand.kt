package kr.hhplus.be.application.coupon

import kr.hhplus.be.presentation.dto.request.CouponIssueRequest

data class CouponIssueCommand(
    val userId: Long,
    val couponId: Long,
) {
    companion object {
        fun of(request: CouponIssueRequest): CouponIssueCommand {
            return CouponIssueCommand(
                userId = request.userId,
                couponId = request.couponId
            )
        }
    }
}
