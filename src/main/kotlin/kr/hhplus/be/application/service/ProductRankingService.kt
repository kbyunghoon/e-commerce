package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductRankingInfo
import kr.hhplus.be.domain.product.ProductRankingRepository
import org.springframework.stereotype.Service

@Service
class ProductRankingService(
    private val productRankingRepository: ProductRankingRepository
) {
    fun getTopProducts(): List<ProductRankingInfo> {
        return productRankingRepository.findTopProducts()
    }
}
