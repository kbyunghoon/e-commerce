package kr.hhplus.be.domain

import java.time.LocalDateTime

data class CouponIssueEvent(
    val userId: Long,
    val couponId: Long,
    val issuedAt: LocalDateTime = LocalDateTime.now()
)