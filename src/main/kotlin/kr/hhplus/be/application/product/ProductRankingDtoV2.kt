package kr.hhplus.be.application.product

import kr.hhplus.be.domain.product.ProductRankingCache
import java.time.LocalDate

class ProductRankingDtoV2 {
    data class ProductRankingInfo(
        val id: Long = 0,
        val productName: String = "",
        val totalSalesCount: Int = 0,
        val rank: Int = 0,
        val rankingDate: LocalDate = LocalDate.now()
    ) {
        companion object {
            fun from(productRanking: ProductRankingCache): ProductRankingInfo {
                return ProductRankingInfo(
                    id = productRanking.productId,
                    totalSalesCount = productRanking.totalSalesCount,
                )
            }
        }
    }
}