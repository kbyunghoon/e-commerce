package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.presentation.api.ProductRankingApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductRankingListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products/top")
class ProductRankingController(
    private val productRankingService: ProductRankingService
) : ProductRankingApi {

    @GetMapping
    override fun getTopProducts(): BaseResponse<ProductRankingListResponse> {
        val response = productRankingService.getTopProducts()

        return BaseResponse.success(ProductRankingListResponse.from(response))
    }
}
