package kr.hhplus.be.infrastructure.persistence.repository

import jakarta.persistence.LockModeType
import kr.hhplus.be.infrastructure.entity.ProductEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") productId: Long): ProductEntity?

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id IN :ids ORDER BY p.id")
    fun findByIdsWithPessimisticLock(@Param("ids") productIds: List<Long>): List<ProductEntity>
}
