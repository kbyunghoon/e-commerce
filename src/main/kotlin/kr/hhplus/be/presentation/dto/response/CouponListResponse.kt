package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.coupon.CouponDto.UserCouponInfo

@Schema(description = "쿠폰 목록 응답")
data class CouponListResponse(
    @field:Schema(description = "사용자 보유 쿠폰 목록")
    val coupons: List<UserCouponInfo>
) {
    companion object {
        fun from(userCoupons: List<UserCouponInfo>): CouponListResponse {
            return CouponListResponse(
                coupons = userCoupons
            )
        }
    }
}
