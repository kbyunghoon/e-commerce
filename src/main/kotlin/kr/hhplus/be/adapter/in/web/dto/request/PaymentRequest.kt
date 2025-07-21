package kr.hhplus.be.adapter.`in`.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 처리 요청")
data class PaymentRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
    
    @field:Schema(description = "결제 방식", example = "BALANCE", required = true, allowableValues = ["BALANCE", "CREDIT_CARD", "BANK_TRANSFER"])
    val paymentMethod: String
)
