package kr.hhplus.be.adapter.`in`.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "쿠폰 목록 응답")
data class CouponListResponse(
    @field:Schema(description = "사용자 보유 쿠폰 목록")
    val coupons: List<CouponResponse>
)
