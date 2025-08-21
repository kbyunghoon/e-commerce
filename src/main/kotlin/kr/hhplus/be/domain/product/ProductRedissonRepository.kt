package kr.hhplus.be.domain.product

import java.time.LocalDate

interface ProductRedissonRepository {
    fun increaseScore(productId: Long, quantity: Int)

    fun getDailyTopProducts(date: LocalDate?): List<ProductRankingCache>

    fun getWeeklyTopProducts(date: LocalDate?): List<ProductRankingCache>

    fun cleanupDailyRanking(date: LocalDate)

    fun cleanupWeeklyRanking(date: LocalDate)
}