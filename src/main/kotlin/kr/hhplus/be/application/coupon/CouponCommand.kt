package kr.hhplus.be.application.coupon

data class CouponIssueCommand(
    val userId: Long,
    val couponId: Long,
)