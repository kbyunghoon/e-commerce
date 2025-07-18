package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "인기 상품 정보")
data class ProductRankingResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val id: Long,
    
    @field:Schema(description = "상품명", example = "아이폰 15")
    val name: String,
    
    @field:Schema(description = "가격", example = "1200000")
    val price: Int,
    
    @field:Schema(description = "총 판매 수량 (최근 3일)", example = "150")
    val totalSalesQuantity: Int,
    
    @field:Schema(description = "순위", example = "1")
    val rank: Int
)
