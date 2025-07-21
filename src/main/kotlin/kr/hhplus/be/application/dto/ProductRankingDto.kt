package kr.hhplus.be.application.dto

data class ProductRankingInfo(
    val productId: Long,
    val productName: String,
    val rank: Int,
    val score: Double,
)
