package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.CreateProductRanking
import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.global.cache.CacheNames
import kr.hhplus.be.global.lock.DistributedLock
import kr.hhplus.be.global.lock.LockResource
import kr.hhplus.be.global.lock.LockStrategy
import kr.hhplus.be.global.lock.ProductRankingLockKeyProvider
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductRankingService(
    private val productRankingRepository: ProductRankingRepository,
) {
    @Cacheable(
        cacheNames = [CacheNames.PRODUCT_RANKING],
        key = "'productRanking:' + #request.rankingDate.toString() + ':' + #request.period.toString()",
    )
    @Transactional(readOnly = true)
    fun getTopProducts(request: ProductRankingCommand): List<ProductRanking> {
        val endDate = request.rankingDate
        val startDate = request.period.getStartDate(endDate)

        return productRankingRepository.findTopProducts(startDate, endDate)
    }

    @DistributedLock(
        resource = LockResource.PRODUCT_RANKING,
        keyProvider = "ProductRankingLockKeyProvider",
        lockStrategy = LockStrategy.PUB_SUB_LOCK,
        waitTime = 5,
        leaseTime = 20
    )
    @Transactional
    fun updateSalesCount(
        command: CreateProductRanking,
        keyProvider: ProductRankingLockKeyProvider = ProductRankingLockKeyProvider(
            command.productId,
            command.rankingDate
        )
    ) {
        val updatedRows =
            productRankingRepository.updateSalesCount(command.productId, command.quantity, command.rankingDate)

        if (updatedRows == 0) {
            // 업데이트된 행이 없으면 새로운 엔티티 생성
            val newEntity = ProductRanking(
                productId = command.productId,
                productName = command.productName,
                totalSalesCount = command.quantity,
                rankingDate = command.rankingDate,
            )
            productRankingRepository.save(newEntity)
        }
    }
}
