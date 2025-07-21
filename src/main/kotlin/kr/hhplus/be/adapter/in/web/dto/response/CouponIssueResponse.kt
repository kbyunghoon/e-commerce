package kr.hhplus.be.adapter.`in`.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.dto.CouponInfo
import kr.hhplus.be.domain.enums.DiscountType
import java.time.LocalDateTime

@Schema(description = "쿠폰 발급 응답")
data class CouponIssueResponse(
    @field:Schema(description = "사용자 쿠폰 ID", example = "1")
    val userCouponId: Long,

    @field:Schema(description = "사용자 ID", example = "1")
    val userId: Long,

    @field:Schema(description = "쿠폰 ID", example = "1")
    val couponId: Long,

    @field:Schema(description = "쿠폰명", example = "10% 할인 쿠폰")
    val couponName: String,

    @field:Schema(description = "할인 타입", example = "PERCENTAGE", allowableValues = ["PERCENTAGE", "FIXED"])
    val discountType: DiscountType,

    @field:Schema(description = "할인 값 (정률: %, 정액: 원)", example = "10")
    val discountValue: Int,

    @field:Schema(description = "만료 일시", example = "2025-02-15T23:59:59")
    val expiresAt: LocalDateTime,

    @field:Schema(description = "발급 일시", example = "2025-01-15T10:30:00")
    val issuedAt: LocalDateTime
) {
    companion object {
        fun from(couponInfo: CouponInfo, userId: Long): CouponIssueResponse {
            return CouponIssueResponse(
                userCouponId = couponInfo.id,
                userId = userId,
                couponId = couponInfo.id,
                couponName = couponInfo.name,
                discountType = couponInfo.discountType,
                discountValue = couponInfo.discountValue,
                expiresAt = couponInfo.expiredAt.atStartOfDay(),
                issuedAt = LocalDateTime.now()
            )
        }
    }
}
