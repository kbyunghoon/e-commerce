package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.product.ProductRanking

@Schema(description = "상품 랭킹 목록 응답")
data class ProductRankingListResponse(
    @field:Schema(description = "상품 랭킹 목록")
    val rankings: List<ProductRankingResponse>
) {

    companion object {
        fun from(dtoList: List<ProductRanking>): ProductRankingListResponse {
            return ProductRankingListResponse(
                rankings = dtoList.map { ProductRankingResponse.from(it) }
            )
        }
    }
}