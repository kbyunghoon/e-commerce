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

    @Column(name = "product_name", nullable = false)
    val productName: String,

    @Column(name = "total_sales_count", nullable = false)
    val totalSalesCount: Int,

    @Column(name = "`rank`", nullable = false)
    val rank: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ranking_date", nullable = false)
    var rankingDate: LocalDate = LocalDate.now()
) {
    fun toDomain(): ProductRanking {
        return ProductRanking(
            productId = id,
            productName = productName,
            totalSalesCount = totalSalesCount,
            rank = rank,
            rankingDate = rankingDate
        )
    }

    companion object {
        fun from(
            productRankingInfo: ProductRanking
        ): ProductRankingEntity {
            return ProductRankingEntity(
                productId = productRankingInfo.productId,
                productName = productRankingInfo.productName,
                totalSalesCount = productRankingInfo.totalSalesCount,
                rank = productRankingInfo.rank,
                rankingDate = productRankingInfo.rankingDate
            )
        }
    }
}