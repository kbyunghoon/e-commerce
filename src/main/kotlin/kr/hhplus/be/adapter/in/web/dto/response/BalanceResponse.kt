package kr.hhplus.be.adapter.`in`.web.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "잔액 정보")
data class BalanceResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val userId: Long,
    
    @field:Schema(description = "현재 잔액", example = "15000")
    val balance: Int
)
