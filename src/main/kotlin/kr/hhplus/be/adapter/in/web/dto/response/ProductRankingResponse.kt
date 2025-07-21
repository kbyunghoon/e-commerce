package kr.hhplus.be.adapter.`in`.web.dto.response

import kr.hhplus.be.application.dto.ProductRankingInfo
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
) {
    companion object {
        fun from(productRankingInfo: ProductRankingInfo): ProductRankingResponse {
            return ProductRankingResponse(
                id = productRankingInfo.productId,
                name = productRankingInfo.productName,
                price = 0, // ProductRankingInfo에 price 정보가 없으므로 임시값
                totalSalesQuantity = 0, // ProductRankingInfo에 totalSalesQuantity 정보가 없으므로 임시값
                rank = productRankingInfo.rank
            )
        }
    }
}
