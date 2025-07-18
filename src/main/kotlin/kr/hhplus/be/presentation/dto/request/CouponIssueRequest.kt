package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "쿠폰 발급 요청")
data class CouponIssueRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
    
    @field:Schema(description = "발급받을 쿠폰 ID", example = "1", required = true)
    val couponId: Long
)
