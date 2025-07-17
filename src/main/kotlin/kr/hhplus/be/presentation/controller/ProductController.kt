package kr.hhplus.be.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.presentation.dto.response.Pagination
import kr.hhplus.be.presentation.dto.response.ProductListResponse
import kr.hhplus.be.presentation.dto.response.ProductResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "상품 관리", description = "상품 목록 조회 및 상세 정보 조회 API")
@RestController
@RequestMapping("/api/v1/products")
class ProductController(private val productService: ProductService) {

    @Operation(
        summary = "상품 목록 조회",
        description = "<h1>판매 가능한 상품 목록 조회</h1><h2>주요 기능</h2>- 상품 조회<br/>- 검색, 가격 필터링"
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
    fun getProducts(
        @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
        @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "페이지 크기", example = "10")
        @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "상품명 검색 키워드", required = false, example = "상품명")
        @RequestParam search: String? = null,
        @Parameter(description = "최소 금액으로 검색", required = false, example = "0")
        @RequestParam minPrice: Int? = null,
        @Parameter(description = "최대 금액으로 검색", required = false, example = "10000")
        @RequestParam maxPrice: Int? = null
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

    @Operation(
        summary = "상품 상세 조회",
        description = "<h1>특정 상품의 상세 정보 조회</h1><h2>주요 기능</h2>- 상품의 모든 상세 정보 반환<br/>- 실시간 재고 수량을 확인 가능<br/>- 존재하지 않는 상품 ID로 조회 시 404 에러 반환"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            ),
            ApiResponse(
                responseCode = "404",
                description = "상품을 찾을 수 없음"
            )
        ]
    )
    @GetMapping("/{productId}")
    fun getProduct(
        @Parameter(description = "상품 ID", required = true, example = "1")
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
