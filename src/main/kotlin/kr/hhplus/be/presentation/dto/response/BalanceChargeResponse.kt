package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "잔액 충전 응답")
data class BalanceChargeResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val userId: Long,
    
    @field:Schema(description = "충전 후 총 잔액", example = "25000")
    val balance: Int,
    
    @field:Schema(description = "충전된 금액", example = "10000")
    val chargedAmount: Int,
    
    @field:Schema(description = "충전 일시", example = "2025-01-15T10:30:00")
    val chargedAt: LocalDateTime
)
