package kr.hhplus.be.domain.product

import java.time.LocalDate

interface ProductRankingRepository {
    fun findTopProducts(startDate: LocalDate, endDate: LocalDate): List<ProductRanking>

    fun updateSalesCount(productId: Long, quantity: Int, date: LocalDate): Int

    fun save(productRanking: ProductRanking): ProductRanking

    fun existsByProductIdAndRankingDate(productId: Long, rankingDate: LocalDate): Boolean

    fun findByProductIdAndRankingDate(productId: Long, rankingDate: LocalDate): ProductRanking?
}
