package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.user.UserCouponDetail

@Schema(description = "쿠폰 목록 응답")
data class UserCouponListResponse(
    @field:Schema(description = "사용자 보유 쿠폰 목록")
    val coupons: List<UserCouponResponse>
) {
    companion object {
        fun from(userCoupons: List<UserCouponDetail>): UserCouponListResponse {
            return UserCouponListResponse(
                coupons = userCoupons.map { UserCouponResponse.from(it) }
            )
        }
    }
}