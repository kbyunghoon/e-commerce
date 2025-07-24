package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "결제 처리 요청")
data class PaymentRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,

    @field:Schema(description = "주문 번호", example = "123", required = true)
    val orderId: Long
)
