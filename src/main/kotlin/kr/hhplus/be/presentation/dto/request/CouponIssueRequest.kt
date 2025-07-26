package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "쿠폰 발급 요청")
data class CouponIssueRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    @field:NotNull(message = "사용자 ID는 필수입니다")
    @field:Positive(message = "사용자 ID는 양수여야 합니다")
    val userId: Long,

    @field:Schema(description = "쿠폰 ID", example = "1", required = true)
    @field:NotNull(message = "쿠폰 ID는 필수입니다")
    @field:Positive(message = "쿠폰 ID는 양수여야 합니다")
    val couponId: Long,
)

