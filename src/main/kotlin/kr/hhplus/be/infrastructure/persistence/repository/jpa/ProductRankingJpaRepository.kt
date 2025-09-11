package kr.hhplus.be.infrastructure.persistence.repository.jpa

import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.infrastructure.entity.ProductRankingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProductRankingJpaRepository : JpaRepository<ProductRankingEntity, Long> {
    @Query(
        "SELECT new kr.hhplus.be.domain.product.ProductRanking(" +
                "pr.productId, p.name, pr.totalSalesCount, pr.rank, pr.rankingDate) " +
                "FROM ProductRankingEntity pr JOIN ProductEntity p ON pr.productId = p.id " +
                "WHERE pr.rankingDate BETWEEN :startDate AND :endDate " +
                "ORDER BY pr.rank ASC"
    )
    fun findTopProducts(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ProductRanking>
}