package kr.hhplus.be.adapter.`in`.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주문 생성 요청")
data class OrderRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    val userId: Long,
    
    @field:Schema(description = "주문할 상품 목록", required = true)
    val items: List<OrderItemRequest>,
    
    @field:Schema(description = "사용할 쿠폰 ID (선택사항)", example = "1", required = false)
    val couponId: Long?
)
