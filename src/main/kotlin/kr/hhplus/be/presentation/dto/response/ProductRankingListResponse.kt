package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "인기 상품 목록 응답")
data class ProductRankingListResponse(
    @field:Schema(description = "인기 상품 목록 (최근 3일 기준 상위 5개)")
    val products: List<ProductRankingResponse>
)
