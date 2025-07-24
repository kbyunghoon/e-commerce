package kr.hhplus.be.infrastructure.persistence.repository

import kr.hhplus.be.infrastructure.entity.ProductStockHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductStockHistoryJpaRepository : JpaRepository<ProductStockHistoryEntity, Long> {
}
