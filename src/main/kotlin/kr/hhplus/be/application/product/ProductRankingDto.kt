package kr.hhplus.be.application.product

import kr.hhplus.be.domain.product.ProductRanking
import java.time.LocalDate

class ProductRankingDto {
    data class ProductRankingInfo(
        val id: Long = 0,
        val productName: String = "",
        val totalSalesCount: Int = 0,
        val rank: Int = 0,
        val rankingDate: LocalDate = LocalDate.now()
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