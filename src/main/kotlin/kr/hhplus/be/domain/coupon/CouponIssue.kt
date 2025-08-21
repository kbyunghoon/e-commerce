package kr.hhplus.be.domain.coupon

data class CouponIssue(
    val userId: Long,
    val couponId: Long,
    val issuedAt: Long
)