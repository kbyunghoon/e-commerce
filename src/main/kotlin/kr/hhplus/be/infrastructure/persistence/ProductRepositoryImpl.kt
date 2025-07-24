package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.infrastructure.persistence.repository.ProductJpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class ProductRepositoryImpl(
    private val productJpaRepository: ProductJpaRepository
) : ProductRepository {

    override fun findById(id: Long): Product? {
        TODO("구현 예정")
    }

    override fun save(product: Product): Product {
        TODO("구현 예정")
    }

    override fun findAvailableProducts(
        pageable: Pageable,
        search: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): Page<Product> {
        TODO("구현 예정")
    }

    override fun findByProductIds(productIds: List<Long>): List<Product> {
        TODO("구현 예정")
    }

    override fun findAll(pageable: Pageable): Page<Product> {
        TODO("구현 예정")
    }

    override fun findByNameContaining(pageable: Pageable, name: String): Page<Product> {
        TODO("구현 예정")
    }

    override fun findByPriceBetween(pageable: Pageable, minPrice: Int, maxPrice: Int): Page<Product> {
        TODO("구현 예정")
    }
}
