package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.infrastructure.persistence.repository.ProductRankingJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductRankingRepositoryImpl(
    private val productRankingJpaRepository: ProductRankingJpaRepository
) : ProductRankingRepository {
    override fun findTopProducts(startDate: LocalDate, endDate: LocalDate): List<ProductRanking> {
        return productRankingJpaRepository.findTopProducts(startDate, endDate)
    }
}
