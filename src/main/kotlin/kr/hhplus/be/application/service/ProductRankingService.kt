package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.application.product.ProductRankingDto
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.global.cache.CacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ProductRankingService(
    private val productRankingRepository: ProductRankingRepository,
) {
    @Cacheable(
        cacheNames = [CacheNames.PRODUCT_RANKING],
        key = "'productRanking:' + #request.rankingDate.toString() + ':' + #request.period.toString()",
    )
    @Transactional(readOnly = true)
    fun getTopProducts(request: ProductRankingCommand): List<ProductRankingDto.ProductRankingInfo> {
        val endDate = request.rankingDate
        val startDate = request.period.getStartDate(endDate)

        return productRankingRepository.findTopProducts(startDate, endDate)
            .map { ProductRankingDto.ProductRankingInfo.from(it) }
    }

    fun updateSalesCount(productId: Long, quantity: Int, date: LocalDate? = LocalDate.now()) {
        productRankingRepository.updateSalesCount(productId, quantity, date!!)
    }
}
