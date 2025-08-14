package kr.hhplus.be.presentation.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.constraints.PastOrPresent
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductRankingListResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.RequestParam
import java.time.LocalDate

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
    fun getTopProducts(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @PastOrPresent(message = "랭킹 날짜는 미래 날짜일 수 없습니다")
        rankingDate: LocalDate?
    ): BaseResponse<ProductRankingListResponse>
}