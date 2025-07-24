package kr.hhplus.be.presentation.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductListResponse
import kr.hhplus.be.presentation.dto.response.ProductResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "상품 관리", description = "상품 목록 조회 및 상세 정보 조회 API")
interface ProductApi {

    @Operation(
        summary = "상품 목록 조회",
        description = "<h1>판매 가능한 상품 목록 조회</h1><h2>주요 기능</h2>- 상품 조회<br/>- 검색, 가격 필터링"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        ]
    )
    fun getProducts(
        @Parameter(description = "페이지네이션")
        @PageableDefault(size = 10) pageable: Pageable,
        @Parameter(description = "상품명 검색 키워드", required = false, example = "상품명")
        @RequestParam search: String? = null,
        @Parameter(description = "최소 금액으로 검색", required = false, example = "0")
        @RequestParam minPrice: Int? = null,
        @Parameter(description = "최대 금액으로 검색", required = false, example = "10000")
        @RequestParam maxPrice: Int? = null
    ): BaseResponse<ProductListResponse>

    @Operation(
        summary = "상품 상세 조회",
        description = "<h1>특정 상품의 상세 정보 조회</h1><h2>주요 기능</h2>- 상품의 모든 상세 정보 반환<br/>- 실시간 재고 수량을 확인 가능<br/>- 존재하지 않는 상품 ID로 조회 시 404 에러 반환"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            ),
            ApiResponse(
                responseCode = "404",
                description = "상품을 찾을 수 없음"
            )
        ]
    )
    fun getProduct(
        @Parameter(description = "상품 ID", required = true, example = "1")
        @PathVariable productId: Long
    ): BaseResponse<ProductResponse>
}