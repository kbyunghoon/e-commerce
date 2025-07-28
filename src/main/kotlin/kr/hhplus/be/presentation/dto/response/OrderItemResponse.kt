package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.order.OrderDto.OrderItemDetails

@Schema(description = "주문 상품 항목")
data class OrderItemResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val productId: Long,
    
    @field:Schema(description = "상품명", example = "아이폰 15")
    val productName: String,
    
    @field:Schema(description = "개당 가격", example = "10000")
    val price: Int,
    
    @field:Schema(description = "주문 수량", example = "2")
    val quantity: Int,
    
    @field:Schema(description = "상품별 총 금액", example = "20000")
    val totalPrice: Int
) {
    companion object {
        fun from(orderItemInfo: OrderItemDetails): OrderItemResponse {
            return OrderItemResponse(
                productId = orderItemInfo.productId,
                productName = "상품명", // TODO: Product 정보를 함께 조회하여 실제 상품명 설정
                price = orderItemInfo.price,
                quantity = orderItemInfo.quantity,
                totalPrice = orderItemInfo.price * orderItemInfo.quantity
            )
        }
    }
}
