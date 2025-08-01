package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.presentation.api.ProductApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.presentation.dto.response.Pagination
import kr.hhplus.be.presentation.dto.response.ProductListResponse
import kr.hhplus.be.presentation.dto.response.ProductResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val productService: ProductService) : ProductApi {

    @GetMapping
    override fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam search: String?,
        @RequestParam minPrice: Int?,
        @RequestParam maxPrice: Int?
    ): ResponseEntity<BaseResponse<ProductListResponse>> {
        val products = productService.getProducts(page, size, search, minPrice, maxPrice)
        val webProducts = products.map { productInfo ->
            ProductResponse(
                id = productInfo.id,
                name = productInfo.name,
                price = productInfo.price,
                stock = productInfo.stock,
                createdAt = productInfo.createdAt,
                updatedAt = productInfo.updatedAt
            )
        }
        val pagination = Pagination(
            page = page,
            size = size,
            totalElements = webProducts.size,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false
        )
        return ResponseEntity.ok(
            BaseResponse.success(
                ProductListResponse(
                    products = webProducts,
                    pagination = pagination
                )
            )
        )
    }

    @GetMapping("/{productId}")
    override fun getProduct(
        @PathVariable productId: Long
    ): BaseResponse<ProductResponse> {
        val productInfo = productService.getProduct(productId)
        return if (productInfo != null) {
            BaseResponse.success(
                ProductResponse(
                    id = productInfo.id,
                    name = productInfo.name,
                    price = productInfo.price,
                    stock = productInfo.stock,
                    createdAt = productInfo.createdAt,
                    updatedAt = productInfo.updatedAt
                )
            )
        } else {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }
    }
}
