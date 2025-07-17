package kr.hhplus.be.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductRankingListResponse
import kr.hhplus.be.presentation.dto.response.ProductRankingResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "통계 및 추천", description = "인기 상품 조회 및 통계 API")
@RestController
@RequestMapping("/api/v1/products/top")
class ProductRankingController(private val productRankingService: ProductRankingService) {

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
    @GetMapping
    fun getTopProducts(): BaseResponse<ProductRankingListResponse> {
        val products = productRankingService.getTopProducts()
        val webProducts = products.map { productRankingInfo ->
            ProductRankingResponse(
                id = productRankingInfo.id,
                name = productRankingInfo.name,
                price = productRankingInfo.price,
                totalSalesQuantity = productRankingInfo.totalSalesQuantity,
                rank = productRankingInfo.rank
            )
        }
        return BaseResponse.success(
            ProductRankingListResponse(
                products = webProducts
            )
        )
    }
}
