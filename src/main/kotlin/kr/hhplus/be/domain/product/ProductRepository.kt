package kr.hhplus.be.domain.product

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun findById(productId: Long): Product?
    fun save(product: Product): Product
    fun saveAll(products: List<Product>): List<Product>
    fun findAvailableProducts(pageable: Pageable, search: String?, minPrice: Int?, maxPrice: Int?): Page<Product>
    fun findByProductIds(productIds: List<Long>): List<Product>
    fun findAll(pageable: Pageable): Page<Product>
    fun findByIdWithPessimisticLock(productId: Long): Product
    fun findByIdsWithPessimisticLock(productIds: List<Long>): List<Product>

    fun findByIdOrThrow(productId: Long): Product {
        return findById(productId) ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
    }
}
