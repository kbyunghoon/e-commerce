package kr.hhplus.be.adapter.`in`.web.dto.response

import kr.hhplus.be.application.dto.ProductInfo
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

@Schema(description = "상품 정보")
data class ProductResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val id: Long,
    
    @field:Schema(description = "상품명", example = "아이폰 15")
    val name: String,
    
    @field:Schema(description = "가격", example = "1200000")
    val price: Int,
    
    @field:Schema(description = "재고 수량", example = "100")
    val stock: Int,
    
    @field:Schema(description = "생성 일시", example = "2025-01-01T00:00:00")
    val createdAt: LocalDateTime,
    
    @field:Schema(description = "수정 일시", example = "2025-01-15T10:30:00")
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(productInfo: ProductInfo): ProductResponse {
            return ProductResponse(
                id = productInfo.id,
                name = productInfo.name,
                price = productInfo.price,
                stock = productInfo.stock,
                createdAt = productInfo.createdAt,
                updatedAt = productInfo.updatedAt
            )
        }
    }
}
