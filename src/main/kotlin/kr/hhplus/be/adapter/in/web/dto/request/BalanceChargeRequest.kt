package kr.hhplus.be.adapter.`in`.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔액 충전 요청")
data class BalanceChargeRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,

    @field:Schema(description = "충전할 금액 (원)", example = "10000", required = true, minimum = "1")
    val amount: Int
)
