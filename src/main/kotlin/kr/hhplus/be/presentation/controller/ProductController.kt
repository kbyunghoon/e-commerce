package kr.hhplus.be.presentation.controller

import jakarta.validation.Valid
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.presentation.api.ProductApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.ProductSearchRequest
import kr.hhplus.be.presentation.dto.response.ProductListResponse
import kr.hhplus.be.presentation.dto.response.ProductResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/products")
class ProductController(
    private val productService: ProductService,
) : ProductApi {

    @GetMapping
    override fun getProducts(
        @PageableDefault(size = 10) pageable: Pageable,
        @Valid request: ProductSearchRequest
    ): BaseResponse<ProductListResponse> {
        val response = productService.getProducts(request.toCommand(pageable))
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
