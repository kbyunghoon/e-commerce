package kr.hhplus.be.infrastructure.persistence.repository.jpa

import kr.hhplus.be.infrastructure.entity.ProductPriceHistoryEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProductPriceHistoryJpaRepository : JpaRepository<ProductPriceHistoryEntity, Long> {
}
