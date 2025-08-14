package kr.hhplus.be.infrastructure.persistence.repository

import kr.hhplus.be.infrastructure.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    @Query(
        "SELECT p FROM ProductEntity p WHERE " +
                "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
                "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
                "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
                "p.stock > 0"
    )
    fun findAvailableProducts(
        pageable: Pageable,
        search: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): Page<ProductEntity>

    fun findByIdIn(ids: List<Long>): List<ProductEntity>
}
