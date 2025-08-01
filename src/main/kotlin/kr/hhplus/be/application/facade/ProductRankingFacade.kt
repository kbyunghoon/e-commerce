package kr.hhplus.be.application.facade

import kr.hhplus.be.application.product.ProductDto.ProductRankingInfo
import kr.hhplus.be.application.service.ProductRankingService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductRankingFacade(
    private val productRankingService: ProductRankingService
) {

    @Transactional(readOnly = true)
    fun getTopProducts(): List<ProductRankingInfo> {
        return productRankingService.getTopProducts()
    }
}
