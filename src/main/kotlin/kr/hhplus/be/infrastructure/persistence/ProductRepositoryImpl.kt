package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.infrastructure.persistence.repository.ProductJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {

    override fun findById(id: Long): Product? {
        return productJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun save(product: Product): Product {
        return productJpaRepository.save(product.toEntity()).toDomain()
    }

    override fun findAvailableProducts(
        pageable: Pageable,
        search: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): Page<Product> {
        return productJpaRepository.findAvailableProducts(pageable, search, minPrice, maxPrice).map { it.toDomain() }
    }

    override fun findByProductIds(productIds: List<Long>): List<Product> {
        return productJpaRepository.findByIdIn(productIds).map { it.toDomain() }
    }

    override fun findAll(pageable: Pageable): Page<Product> {
        return productJpaRepository.findAll(pageable).map { it.toDomain() }
    }
}
