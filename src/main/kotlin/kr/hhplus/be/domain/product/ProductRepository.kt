package kr.hhplus.be.domain.product

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductRepository {
    fun findById(id: Long): Product?
    fun save(product: Product): Product
    fun findAvailableProducts(pageable: Pageable, search: String?, minPrice: Int?, maxPrice: Int?): Page<Product>
    fun findByProductIds(productIds: List<Long>): List<Product>
    fun findAll(pageable: Pageable): Page<Product>
    fun findByNameContaining(pageable: Pageable, name: String): Page<Product>
    fun findByPriceBetween(pageable: Pageable, minPrice: Int, maxPrice: Int): Page<Product>
}
