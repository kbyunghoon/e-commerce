package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.enums.OrderStatus

@Schema(description = "주문 생성 응답")
data class OrderResponse(
    @field:Schema(description = "임시 주문 ID", example = "temp-order-123")
    val orderId: String,
    
    @field:Schema(description = "사용자 ID", example = "1")
    val userId: Long,
    
    @field:Schema(description = "주문 상품 목록")
    val items: List<OrderItemResponse>,
    
    @field:Schema(description = "할인 전 총액", example = "20000")
    val originalAmount: Int,
    
    @field:Schema(description = "할인 금액", example = "2000")
    val discountAmount: Int,
    
    @field:Schema(description = "최종 결제 금액", example = "18000")
    val finalAmount: Int,
    
    @field:Schema(description = "주문 상태", example = "PENDING", allowableValues = ["PENDING", "COMPLETED", "CANCELLED"])
    val status: OrderStatus
)
