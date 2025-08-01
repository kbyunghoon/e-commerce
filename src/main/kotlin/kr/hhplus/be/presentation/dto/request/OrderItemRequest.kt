package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.domain.order.OrderItem

@Schema(description = "주문 상품 요청")
data class OrderItemRequest(
    @field:Schema(description = "상품 ID", example = "1", required = true)
    @field:NotNull(message = "상품 ID는 필수입니다")
    @field:Positive(message = "상품 ID는 양수여야 합니다")
    val productId: Long,
    
    @field:Schema(description = "주문 수량", example = "2", required = true)
    @field:NotNull(message = "주문 수량은 필수입니다")
    @field:Min(value = OrderItem.MIN_QUANTITY.toLong(), 
               message = "주문 수량은 최소 ${OrderItem.MIN_QUANTITY}개 이상이어야 합니다")
    @field:Max(value = OrderItem.MAX_QUANTITY.toLong(), 
               message = "주문 수량은 최대 ${OrderItem.MAX_QUANTITY}개 이하여야 합니다")
    val quantity: Int
) {
    fun toCommand(): OrderItemCreateCommand {
        return OrderItemCreateCommand(
            productId = this.productId,
            quantity = this.quantity
        )
    }
}
