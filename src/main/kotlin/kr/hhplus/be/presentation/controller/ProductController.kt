package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.application.service.ProductService
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
    private val productService: ProductService,
    private val productRankingService: ProductRankingService
) : ProductApi {

    @GetMapping
    override fun getProducts(
        @PageableDefault(size = 10) pageable: Pageable,
        @RequestParam search: String?,
        @RequestParam minPrice: Int?,
        @RequestParam maxPrice: Int?
    ): BaseResponse<ProductListResponse> {
        val response = when {
            !search.isNullOrBlank() -> productService.searchProductsByName(pageable, search)
            minPrice != null && maxPrice != null -> productService.getProductsByPriceRange(pageable, minPrice, maxPrice)
            else -> productService.getAllProducts(pageable)
        }
        return BaseResponse.success(ProductListResponse.from(response))
    }

    @GetMapping("/{productId}")
    override fun getProduct(
        @PathVariable productId: Long
    ): BaseResponse<ProductResponse> {
        val response = productService.getProduct(productId)

        return BaseResponse.success(ProductResponse.from(response))
    }
}
