package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.application.product.ProductRankingDto
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.domain.product.ProductRedissonRepository
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.global.cache.CacheNames
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class ProductRankingService(
    private val productRankingRepository: ProductRankingRepository,
    private val productRedissonRepository: ProductRedissonRepository,
    private val productRepository: ProductRepository
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

    fun updateSalesCount(productId: Long, quantity: Int) {
        productRankingRepository.updateSalesCount(productId, quantity)
    }

    fun cleanupDailyRanking(yesterday: LocalDate) {
        productRedissonRepository.cleanupDailyRanking(yesterday)
    }

    fun cleanupWeeklyRanking(yesterday: LocalDate) {
        productRedissonRepository.cleanupWeeklyRanking(yesterday)
    }
}
