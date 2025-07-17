package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "잔액 조회 응답")
data class BalanceQueryResponse(
    @field:Schema(description = "사용자 ID", example = "1")
    val userId: Long,
    
    @field:Schema(description = "현재 잔액", example = "15000")
    val balance: Int,
    
    @field:Schema(description = "마지막 업데이트 일시", example = "2025-01-15T10:30:00")
    val lastUpdatedAt: LocalDateTime
)
