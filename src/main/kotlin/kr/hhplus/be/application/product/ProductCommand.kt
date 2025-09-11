package kr.hhplus.be.application.product

import kr.hhplus.be.domain.product.RankingPeriod
import java.time.LocalDate

data class ProductRankingCommand(
    val rankingDate: LocalDate,
    val period: RankingPeriod,
)

data class CreateProductRanking(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val rankingDate: LocalDate
)