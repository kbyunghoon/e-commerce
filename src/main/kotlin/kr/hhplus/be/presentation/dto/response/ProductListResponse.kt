package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "상품 목록 응답")
data class ProductListResponse(
    @field:Schema(description = "상품 목록")
    val products: List<ProductResponse>,
    
    @field:Schema(description = "페이지네이션 정보")
    val pagination: Pagination
)

@Schema(description = "페이지네이션 정보")
data class Pagination(
    @field:Schema(description = "현재 페이지 번호 (0부터 시작)", example = "0")
    val page: Int,
    
    @field:Schema(description = "페이지 크기", example = "10")
    val size: Int,
    
    @field:Schema(description = "전체 요소 수", example = "50")
    val totalElements: Int,
    
    @field:Schema(description = "전체 페이지 수", example = "5")
    val totalPages: Int,
    
    @field:Schema(description = "다음 페이지 존재 여부", example = "true")
    val hasNext: Boolean,
    
    @field:Schema(description = "이전 페이지 존재 여부", example = "false")
    val hasPrevious: Boolean
)
