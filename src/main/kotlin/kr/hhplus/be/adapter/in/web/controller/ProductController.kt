package kr.hhplus.be.adapter.`in`.web.controller

import kr.hhplus.be.adapter.`in`.web.api.ProductApi
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.Pagination
import kr.hhplus.be.adapter.`in`.web.dto.response.ProductListResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.ProductResponse
import kr.hhplus.be.application.port.`in`.ProductUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val productUseCase: ProductUseCase) : ProductApi {

    @GetMapping
    override fun getProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam search: String?,
        @RequestParam minPrice: Int?,
        @RequestParam maxPrice: Int?
    ): ResponseEntity<BaseResponse<ProductListResponse>> {
        val productInfos = productUseCase.getProducts()
        val webProducts = productInfos.map { productInfo ->
            ProductResponse.from(productInfo)
        }
        // TODO: 현재 ProductUseCase는 필터링/페이지네이션을 처리 X, 임시로 전체 목록을 반환하고 페이지네이션 정보 단순화
        val pagination = Pagination(
            page = 0,
            size = webProducts.size,
            totalElements = webProducts.size.toLong(),
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
        val productInfo = productUseCase.getProduct(productId)
        return BaseResponse.success(
            ProductResponse.from(productInfo)
        )
    }
}

