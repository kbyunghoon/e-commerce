package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductDto.ProductRankingInfo
import kr.hhplus.be.domain.product.ProductRankingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductRankingService(
    private val productRankingRepository: ProductRankingRepository
) {
    @Transactional(readOnly = true)
    fun getTopProducts(): List<ProductRankingInfo> {
        return productRankingRepository.findTopProducts()
    }
}
