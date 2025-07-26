package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "잔액 충전 요청")
data class BalanceChargeRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    @field:NotNull(message = "사용자 ID는 필수입니다")
    @field:Positive(message = "사용자 ID는 양수여야 합니다")
    val userId: Long,

    @field:Schema(description = "충전할 금액 (원)", example = "10000", required = true)
    @field:NotNull(message = "충전 금액은 필수입니다")
    @field:Positive(message = "충전 금액은 양수여야 합니다")
    val amount: Int,
)