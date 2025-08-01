package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.coupon.CouponDto.UserCouponInfo
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import java.time.LocalDateTime

@Schema(description = "쿠폰 정보")
data class CouponResponse(
    @field:Schema(description = "사용자 쿠폰 ID", example = "1")
    val id: Long,

    @field:Schema(description = "쿠폰 ID", example = "1")
    val couponId: Long,

    @field:Schema(description = "쿠폰명", example = "10% 할인 쿠폰")
    val couponName: String,

    @field:Schema(description = "할인 타입", example = "PERCENTAGE", allowableValues = ["PERCENTAGE", "FIXED"])
    val discountType: DiscountType,

    @field:Schema(description = "할인 값 (정률: %, 정액: 원)", example = "10")
    val discountValue: Int,

    @field:Schema(description = "쿠폰 상태", example = "AVAILABLE", allowableValues = ["AVAILABLE", "USED", "EXPIRED"])
    val status: CouponStatus,

    @field:Schema(description = "만료 일시", example = "2025-02-15T23:59:59")
    val expiresAt: LocalDateTime,

    @field:Schema(description = "발급 일시", example = "2025-01-15T10:30:00")
    val issuedAt: LocalDateTime
) {
    companion object {
        fun from(userCouponInfo: UserCouponInfo): CouponResponse {
            return CouponResponse(
                id = userCouponInfo.id,
                couponId = userCouponInfo.couponId,
                couponName = userCouponInfo.couponName,
                discountType = userCouponInfo.discountType,
                discountValue = userCouponInfo.discountValue,
                status = userCouponInfo.status,
                expiresAt = userCouponInfo.expiresAt,
                issuedAt = userCouponInfo.issuedAt
            )
        }
    }
}
