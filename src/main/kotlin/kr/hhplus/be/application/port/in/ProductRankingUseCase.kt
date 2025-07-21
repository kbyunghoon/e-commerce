package kr.hhplus.be.application.port.`in`

import kr.hhplus.be.application.dto.ProductRankingInfo

interface ProductRankingUseCase {
    fun getTopProducts(limit: Int): List<ProductRankingInfo>
}
