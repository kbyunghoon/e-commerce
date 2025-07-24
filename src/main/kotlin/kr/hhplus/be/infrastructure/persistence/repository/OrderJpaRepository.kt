package kr.hhplus.be.infrastructure.persistence.repository

import kr.hhplus.be.infrastructure.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderJpaRepository : JpaRepository<OrderEntity, Long> {
}
