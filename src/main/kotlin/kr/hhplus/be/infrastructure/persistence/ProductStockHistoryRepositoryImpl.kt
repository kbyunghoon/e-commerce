package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.product.ProductStockHistory
import kr.hhplus.be.domain.product.ProductStockHistoryRepository
import kr.hhplus.be.infrastructure.entity.ProductStockHistoryEntity
import kr.hhplus.be.infrastructure.persistence.repository.ProductStockHistoryJpaRepository
import org.springframework.stereotype.Component

@Component
class ProductStockHistoryRepositoryImpl(
    private val productStockHistoryJpaRepository: ProductStockHistoryJpaRepository
) : ProductStockHistoryRepository {

    override fun save(productHistory: ProductStockHistory): ProductStockHistory {
        return productStockHistoryJpaRepository.save(ProductStockHistoryEntity.from(productHistory)).toDomain()
    }
}
