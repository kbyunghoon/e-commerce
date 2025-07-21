package kr.hhplus.be.adapter.`in`.web.controller

import kr.hhplus.be.application.port.`in`.ProductRankingUseCase
import kr.hhplus.be.adapter.`in`.web.api.ProductRankingApi
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.ProductRankingListResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.ProductRankingResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products/top")
class ProductRankingController(private val productRankingUseCase: ProductRankingUseCase) : ProductRankingApi {

    @GetMapping
    override fun getTopProducts(@RequestParam(defaultValue = "10") limit: Int): BaseResponse<ProductRankingListResponse> {
        val productRankingInfos = productRankingUseCase.getTopProducts(limit)
        val webProducts = productRankingInfos.map { productRankingInfo ->
            ProductRankingResponse.from(productRankingInfo)
        }
        return BaseResponse.success(
            ProductRankingListResponse(
                products = webProducts
            )
        )
    }
}
