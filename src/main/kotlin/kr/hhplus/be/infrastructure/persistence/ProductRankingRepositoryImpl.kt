package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.application.product.ProductDto.ProductRankingInfo
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.infrastructure.persistence.repository.ProductJpaRepository
import org.springframework.stereotype.Component

@Component
class ProductRankingRepositoryImpl(
    private val productRankingJpaRepository: ProductJpaRepository
) : ProductRankingRepository {
    override fun findTopProducts(): List<ProductRankingInfo> {
        TODO("구현 예정")
    }
}
