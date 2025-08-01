package kr.hhplus.be.application.facade

import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.application.service.ProductService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductFacade(
    private val productService: ProductService
) {

    @Transactional(readOnly = true)
    fun getProducts(
        pageable: Pageable,
        searchKeyword: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): Page<ProductDto.ProductInfo> {
        return when {
            !searchKeyword.isNullOrBlank() -> productService.searchProductsByName(pageable, searchKeyword)
            minPrice != null && maxPrice != null -> productService.getProductsByPriceRange(pageable, minPrice, maxPrice)
            else -> productService.getAllProducts(pageable)
        }
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: Long): ProductDto.ProductInfo {
        return productService.getProduct(productId)
    }
}
