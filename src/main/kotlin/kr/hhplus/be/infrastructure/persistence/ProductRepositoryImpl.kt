package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.infrastructure.entity.ProductEntity
import kr.hhplus.be.infrastructure.persistence.repository.ProductJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {

    override fun findById(productId: Long): Product? {
        return productJpaRepository.findByIdOrNull(productId)?.toDomain()
    }

    override fun save(product: Product): Product {
        return productJpaRepository.save(ProductEntity.from(product)).toDomain()
    }

    override fun saveAll(products: List<Product>): List<Product> {
        val entities = products.map { ProductEntity.from(it) }
        return productJpaRepository.saveAll(entities).map { it.toDomain() }
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

    override fun findByIdWithPessimisticLock(productId: Long): Product {
        return productJpaRepository.findByIdWithPessimisticLock(productId)?.toDomain()
            ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
    }

    override fun findByIdsWithPessimisticLock(productIds: List<Long>): List<Product> {
        val sortedIds = productIds.sorted()
        return productJpaRepository.findByIdsWithPessimisticLock(sortedIds).map { it.toDomain() }
    }
}
