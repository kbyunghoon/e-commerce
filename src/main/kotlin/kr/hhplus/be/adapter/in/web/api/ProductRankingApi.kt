package kr.hhplus.be.adapter.`in`.web.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.ProductRankingListResponse

import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "통계 및 추천", description = "인기 상품 조회 및 통계 API")
interface ProductRankingApi {

    @Operation(
        summary = "인기 상품 조회",
        description = "<h1>인기 상품 조회</h1><h2>주요 기능</h2>- 최근 3일간 가장 많이 판매된 상위 5개 상품 조회<br/>- 각 상품의 총 판매 수량과 순위를 포함"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        ]
    )
    fun getTopProducts(@RequestParam(defaultValue = "10") limit: Int): BaseResponse<ProductRankingListResponse>
}