package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Schema(description = "주문 응답")
data class OrderResponse(
    @field:Schema(description = "주문 ID", example = "1")
    val id: Long,

    @field:Schema(description = "주문 번호", example = "T241225143045XX")
    val orderNumber: String,

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
    val orderDate: LocalDateTime?
) {
    companion object {
        fun from(order: Order): OrderResponse {
            return OrderResponse(
                id = order.id ?: 0L,
                orderNumber = order.orderNumber,
                userId = order.userId,
                items = order.orderItems!!.map { OrderItemResponse.from(it) },
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                finalAmount = order.finalAmount,
                status = order.status,
                orderDate = order.orderDate
            )
        }
    }
}