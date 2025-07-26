package kr.hhplus.be.presentation.dto.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

@Schema(description = "주문 생성 요청")
data class OrderRequest(
    @field:Schema(description = "사용자 ID", example = "1", required = true)
    @field:NotNull(message = "사용자 ID는 필수입니다")
    @field:Positive(message = "사용자 ID는 양수여야 합니다")
    val userId: Long,

    @field:Schema(description = "주문할 상품 목록", required = true)
    @field:NotEmpty(message = "주문 상품 목록은 비어있을 수 없습니다")
    @field:Valid
    val items: List<OrderItemRequest>,

    @field:Schema(description = "사용할 쿠폰 ID", example = "1", required = false)
    @field:Positive(message = "쿠폰 ID는 양수여야 합니다")
    val couponId: Long?
)
