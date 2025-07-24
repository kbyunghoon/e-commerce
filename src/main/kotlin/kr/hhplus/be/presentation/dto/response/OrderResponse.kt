package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.order.OrderDto.OrderInfo
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Schema(description = "주문 응답")
data class OrderResponse(
    @field:Schema(description = "주문 ID", example = "1")
    val orderId: Long,

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
    val status: OrderStatus,

    @field:Schema(description = "주문 일시")
    val orderedAt: LocalDateTime
) {
    companion object {
        fun from(orderInfo: OrderInfo): OrderResponse {
            val webOrderItems = orderInfo.orderItems.map { OrderItemResponse.from(it) }
            return OrderResponse(
                orderId = orderInfo.id ?: 0L,
                userId = orderInfo.userId,
                items = webOrderItems,
                originalAmount = orderInfo.originalAmount,
                discountAmount = orderInfo.discountAmount,
                finalAmount = orderInfo.finalAmount,
                status = orderInfo.status,
                orderedAt = orderInfo.orderedAt
            )
        }
    }
}
