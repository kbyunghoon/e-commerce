package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.facade.ProductRankingFacade
import kr.hhplus.be.presentation.api.ProductRankingApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductRankingListResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products/top")
class ProductRankingController(
    private val productRankingFacade: ProductRankingFacade
) : ProductRankingApi {

    @GetMapping
    override fun getTopProducts(): BaseResponse<ProductRankingListResponse> {
        val response = productRankingFacade.getTopProducts()

        return BaseResponse.success(ProductRankingListResponse.from(response))
    }
}
