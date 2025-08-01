package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.facade.ProductFacade
import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.presentation.api.ProductApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductListResponse
import kr.hhplus.be.presentation.dto.response.ProductResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productFacade: ProductFacade,
    private val productRankingService: ProductRankingService
) : ProductApi {

    @GetMapping
    override fun getProducts(
        @PageableDefault(size = 10) pageable: Pageable,
        @RequestParam search: String?,
        @RequestParam minPrice: Int?,
        @RequestParam maxPrice: Int?
    ): BaseResponse<ProductListResponse> {
        val response = productFacade.getProducts(pageable, search, minPrice, maxPrice)
        return BaseResponse.success(ProductListResponse.from(response))
    }

    @GetMapping("/{productId}")
    override fun getProduct(
        @PathVariable productId: Long
    ): BaseResponse<ProductResponse> {
        val response = productFacade.getProduct(productId)

        return BaseResponse.success(ProductResponse.from(response))
    }
}
