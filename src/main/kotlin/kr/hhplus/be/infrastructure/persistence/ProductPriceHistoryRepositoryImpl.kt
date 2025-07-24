package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.ProductPriceHistoryRepository
import kr.hhplus.be.infrastructure.persistence.repository.ProductPriceHistoryJpaRepository
import org.springframework.stereotype.Component

@Component
class ProductPriceHistoryRepositoryImpl(
    private val productPriceHistoryJpaRepository: ProductPriceHistoryJpaRepository
) : ProductPriceHistoryRepository {
}
