package kr.hhplus.be.adapter.`in`.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.dto.CouponInfo
import kr.hhplus.be.domain.enums.CouponStatus
import kr.hhplus.be.domain.enums.DiscountType
import java.time.LocalDateTime

@Schema(description = "쿠폰 정보")
data class CouponResponse(
    @field:Schema(description = "사용자 쿠폰 ID", example = "1")
    val userCouponId: Long,

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
    val expiryDate: LocalDateTime,

    @field:Schema(description = "발급 일시", example = "2025-01-15T10:30:00")
    val issuedAt: LocalDateTime
) {
    companion object {
        fun from(couponInfo: CouponInfo): CouponResponse {
            return CouponResponse(
                userCouponId = couponInfo.id,
                couponId = couponInfo.id,
                couponName = couponInfo.name,
                discountType = couponInfo.discountType,
                discountValue = couponInfo.discountValue,
                status = couponInfo.status,
                expiryDate = couponInfo.expiredAt.atStartOfDay(),
                issuedAt = LocalDateTime.now()
            )
        }
    }
}
