package kr.hhplus.be.domain.product

import java.time.LocalDate

data class ProductRanking(
    val productId: Long,
    val productName: String,
    val totalSalesCount: Int,
    val rankingDate: LocalDate
) {
    constructor(productId: Long, name: String, totalSalesCountLong: Long, rankingDate: LocalDate) :
            this(productId, name, totalSalesCountLong.toInt(), rankingDate)
}


data class ProductRankingCache(
    val productId: Long,
    val totalSalesCount: Int,
)