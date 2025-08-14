package kr.hhplus.be.domain.product

import java.time.LocalDate

interface ProductRankingRepository {
    fun findTopProducts(startDate: LocalDate, endDate: LocalDate): List<ProductRanking>
}
