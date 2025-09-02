package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.infrastructure.entity.ProductRankingEntity
import kr.hhplus.be.infrastructure.persistence.repository.jpa.ProductRankingJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Component
class ProductRankingRepositoryImpl(
    private val productRankingJpaRepository: ProductRankingJpaRepository
) : ProductRankingRepository {
    override fun findTopProducts(startDate: LocalDate, endDate: LocalDate): List<ProductRanking> {
        return productRankingJpaRepository.findTopProducts(startDate, endDate)
    }

    @Transactional
    override fun updateSalesCount(productId: Long, quantity: Int, date: LocalDate): Int {
        return productRankingJpaRepository.updateSalesCount(productId, quantity, date)
    }

    override fun save(productRanking: ProductRanking): ProductRanking {
        return productRankingJpaRepository.save(ProductRankingEntity.from(productRanking)).toDomain()
    }

    override fun existsByProductIdAndRankingDate(
        productId: Long,
        rankingDate: LocalDate
    ): Boolean {
        return productRankingJpaRepository.existsByProductIdAndRankingDate(productId, rankingDate)
    }

    override fun findByProductIdAndRankingDate(
        productId: Long,
        rankingDate: LocalDate
    ): ProductRanking? {
        return productRankingJpaRepository.findByProductIdAndRankingDate(productId, rankingDate)
    }
}
