package kr.hhplus.be.adapter.`in`.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.enums.DiscountType
import java.time.LocalDate

@Schema(description = "쿠폰 발급 요청")
data class CouponIssueRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,

    @field:Schema(description = "쿠폰 이름", example = "10% 할인 쿠폰", required = true)
    val name: String,

    @field:Schema(description = "할인 타입 (PERCENTAGE, FIXED)", example = "PERCENTAGE", required = true)
    val discountType: DiscountType,

    @field:Schema(description = "할인 값", example = "10", required = true)
    val discountValue: Int,

    @field:Schema(description = "발급 수량", example = "1", required = true)
    val issueCount: Int,

    @field:Schema(description = "만료일 (YYYY-MM-DD)", example = "2025-12-31", required = true)
    val expiredAt: LocalDate,
)
