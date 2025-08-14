package kr.hhplus.be.domain.product

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun findById(id: Long): Product?
    fun save(product: Product): Product
    fun findAvailableProducts(pageable: Pageable, search: String?, minPrice: Int?, maxPrice: Int?): Page<Product>
    fun findByProductIds(productIds: List<Long>): List<Product>
    fun findAll(pageable: Pageable): Page<Product>

    fun findByIdOrThrow(id: Long): Product {
        return findById(id) ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
    }
}
