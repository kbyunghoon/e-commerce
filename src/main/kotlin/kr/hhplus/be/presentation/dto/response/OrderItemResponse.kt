package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "주문 상품 항목")
data class OrderItemResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val productId: Long,
    
    @field:Schema(description = "상품명", example = "아이폰 15")
    val productName: String,
    
    @field:Schema(description = "개당 가격", example = "10000")
    val price: Int,
    
    @field:Schema(description = "주문 수량", example = "2")
    val quantity: Int
)
