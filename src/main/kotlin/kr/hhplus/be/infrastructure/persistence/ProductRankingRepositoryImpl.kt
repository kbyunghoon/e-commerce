package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.infrastructure.entity.ProductRankingEntity
import kr.hhplus.be.infrastructure.persistence.repository.jpa.ProductRankingJpaRepository
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class ProductRankingRepositoryImpl(
    private val productRankingJpaRepository: ProductRankingJpaRepository
) : ProductRankingRepository {
    override fun findTopProducts(startDate: LocalDate, endDate: LocalDate): List<ProductRanking> {
        return productRankingJpaRepository.findTopProducts(startDate, endDate)
    }

    override fun saveAll(products: List<ProductRanking>): List<ProductRanking> {
        return productRankingJpaRepository.saveAll(products.map { ProductRankingEntity.from(it) }).map { it.toDomain() }
    }
}
