package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.product.ProductRanking
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "product_rankings")
class ProductRankingEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ranking_id")
    val id: Long = 0,

    @Column(name = "product_id", nullable = false, unique = true)
    val productId: Long,

    @Column(name = "total_sales_count", nullable = false)
    val totalSalesCount: Int,

    @Column(name = "`rank`", nullable = false, unique = true)
    val rank: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ranking_date", nullable = false)
    var rankingDate: LocalDate = LocalDate.now()
) {
    companion object {
        fun from(
            productRankingInfo: ProductRanking
        ): ProductRankingEntity {
            return ProductRankingEntity(
                productId = productRankingInfo.productId,
                totalSalesCount = productRankingInfo.totalSalesCount,
                rank = productRankingInfo.rank,
                rankingDate = productRankingInfo.rankingDate
            )
        }
    }
}