package kr.hhplus.be.application.product

import kr.hhplus.be.domain.product.ProductRanking
import java.time.LocalDate

class ProductRankingDto {
    data class ProductRankingInfo(
        val id: Long,
        val productName: String,
        val totalSalesCount: Int,
        val rank: Int,
        val rankingDate: LocalDate
    ) {
        companion object {
            fun from(productRanking: ProductRanking): ProductRankingInfo {
                return ProductRankingInfo(
                    id = productRanking.productId,
                    productName = productRanking.productName,
                    totalSalesCount = productRanking.totalSalesCount,
                    rank = productRanking.rank,
                    rankingDate = productRanking.rankingDate,
                )
            }
        }
    }
}