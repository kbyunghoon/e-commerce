package kr.hhplus.be.adapter.`in`.web.dto.request

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주문 상품 항목")
data class OrderItemRequest(
    @field:Schema(description = "상품 ID", example = "1", required = true)
    val productId: Long,
    
    @field:Schema(description = "주문 수량", example = "2", required = true, minimum = "1")
    val quantity: Int
)
