package kr.hhplus.be.domain.product

import kr.hhplus.be.application.product.ProductRankingInfo

interface ProductRankingRepository {
    fun findTopProducts(): List<ProductRankingInfo>
}
