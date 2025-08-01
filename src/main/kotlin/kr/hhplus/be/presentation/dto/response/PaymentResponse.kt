package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Schema(description = "결제 처리 응답")
data class PaymentResponse(
    @field:Schema(description = "주문 ID", example = "12345")
    val orderId: Long,

    @field:Schema(description = "주문 번호", example = "order-20250115-001")
    val orderNumber: String,

    @field:Schema(description = "사용자 ID", example = "1")
    val userId: Long,

    @field:Schema(description = "최종 결제 금액", example = "18000")
    val finalAmount: Int,

    @field:Schema(description = "주문 상태", example = "COMPLETED", allowableValues = ["PENDING", "COMPLETED", "CANCELLED"])
    val status: OrderStatus,

    @field:Schema(description = "주문 완료 일시", example = "2025-01-15T10:30:00")
    val orderedAt: LocalDateTime
) {
    companion object {
        fun from(orderResponse: OrderResponse): PaymentResponse {
            return PaymentResponse(
                orderId = orderResponse.orderId,
                orderNumber = orderResponse.orderId.toString(), // TODO: 주문 번호는 임시로 orderId로 설정
                userId = orderResponse.userId,
                finalAmount = orderResponse.finalAmount,
                status = orderResponse.status,
                orderedAt = orderResponse.orderedAt
            )
        }
    }
}
