package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.application.product.ProductRankingDtoV1
import kr.hhplus.be.application.product.ProductRankingDtoV2
import kr.hhplus.be.domain.product.ProductRanking
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
        key = "'productRanking:' + #request.rankingDate.toString()"
    )
    @Transactional(readOnly = true)
    fun getTopProductsV2(request: ProductRankingCommand): List<ProductRankingDtoV2.ProductRankingInfo> {
        return productRedissonRepository.getDailyTopProducts(request.rankingDate).map {
            ProductRankingDtoV2.ProductRankingInfo.from(it)
        }
    }

    @Cacheable(
        cacheNames = [CacheNames.PRODUCT_RANKING],
        key = "'productRanking:' + #request.rankingDate.toString()"
    )
    @Transactional(readOnly = true)
    fun getTopProductsV1(request: ProductRankingCommand): List<ProductRankingDtoV1.ProductRankingInfo> {
        val endDate = request.rankingDate
        val startDate = request.period.getStartDate(endDate)

        return productRankingRepository.findTopProducts(startDate, endDate)
            .map { ProductRankingDtoV1.ProductRankingInfo.from(it) }
    }

    fun increaseProductStockCache(productId: Long, quantity: Int) {
        productRedissonRepository.increaseScore(productId, quantity)
    }

    @Transactional
    fun backupDailyRanking(yesterday: LocalDate) {
        val productRankingCache = productRedissonRepository.getDailyTopProducts(yesterday)

        val entities = productRankingCache.mapIndexed { index, cache ->
            val productName = productRepository.findByIdOrThrow(cache.productId).name
            ProductRanking(
                productId = cache.productId,
                productName = productName,
                totalSalesCount = cache.totalSalesCount,
                rank = index + 1,
                rankingDate = yesterday,
            )
        }

        productRankingRepository.saveAll(entities)
    }

    fun cleanupDailyRanking(yesterday: LocalDate) {
        productRedissonRepository.cleanupDailyRanking(yesterday)
    }

    fun cleanupWeeklyRanking(yesterday: LocalDate) {
        productRedissonRepository.cleanupWeeklyRanking(yesterday)
    }
}
