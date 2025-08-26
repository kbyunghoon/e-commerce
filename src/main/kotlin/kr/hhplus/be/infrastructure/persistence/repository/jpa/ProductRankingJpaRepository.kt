package kr.hhplus.be.infrastructure.persistence.repository.jpa

import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.infrastructure.entity.ProductRankingEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface ProductRankingJpaRepository : JpaRepository<ProductRankingEntity, Long> {
    @Query(
        "SELECT new kr.hhplus.be.domain.product.ProductRanking(" +
                "pr.productId, p.name, SUM(pr.totalSalesCount), MAX(pr.rankingDate)) " +
                "FROM ProductRankingEntity pr JOIN ProductEntity p ON pr.productId = p.id " +
                "WHERE pr.rankingDate BETWEEN :startDate AND :endDate " +
                "GROUP BY pr.productId, p.name " +
                "ORDER BY SUM(pr.totalSalesCount) DESC"
    )
    fun findTopProducts(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate
    ): List<ProductRanking>


    @Modifying
    @Query("UPDATE ProductRankingEntity pr SET pr.totalSalesCount = pr.totalSalesCount + :quantity WHERE pr.productId = :productId AND pr.rankingDate = :date")
    fun updateSalesCount(@Param("productId") productId: Long, @Param("quantity") quantity: Int, @Param("date") today: LocalDate)
}