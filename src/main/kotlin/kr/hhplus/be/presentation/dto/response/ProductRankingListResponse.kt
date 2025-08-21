package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.product.ProductRankingDtoV2

@Schema(description = "상품 랭킹 목록 응답")
data class ProductRankingListResponse(
    @field:Schema(description = "상품 랭킹 목록")
    val rankings: List<ProductRankingResponse>
) {

    companion object {
        fun from(dtoList: List<ProductRankingDtoV2.ProductRankingInfo>): ProductRankingListResponse {
            return ProductRankingListResponse(
                rankings = dtoList.map { ProductRankingResponse.from(it) }
            )
        }
    }
}