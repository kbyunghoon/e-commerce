package kr.hhplus.be.domain.product

import java.time.LocalDate

data class ProductRanking(
    val productId: Long,
    val productName: String,
    val totalSalesCount: Int,
    val rank: Int,
    val rankingDate: LocalDate
)

data class ProductRankingCache(
    val productId: Long,
    val totalSalesCount: Int,
)