package kr.hhplus.be.infrastructure.persistence.repository.jpa

import kr.hhplus.be.infrastructure.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemJpaRepository : JpaRepository<OrderItemEntity, Long> {
    fun findByOrderId(orderId: Long): List<OrderItemEntity>
}
