package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "결제 처리 요청")
data class PaymentRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    @field:NotNull(message = "사용자 ID는 필수입니다")
    @field:Positive(message = "사용자 ID는 양수여야 합니다")
    val userId: Long,

    @field:Schema(description = "주문 번호", example = "123", required = true)
    @field:NotNull(message = "주문 번호는 필수입니다")
    val orderId: Long
)
