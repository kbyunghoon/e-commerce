package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderItemRepository
import kr.hhplus.be.infrastructure.entity.OrderItemEntity
import kr.hhplus.be.infrastructure.persistence.repository.jpa.OrderItemJpaRepository
import org.springframework.stereotype.Component

@Component
class OrderItemRepositoryImpl(
    private val orderItemJpaRepository: OrderItemJpaRepository
) : OrderItemRepository {
    override fun saveAll(items: List<OrderItem>): List<OrderItem> {
        return orderItemJpaRepository.saveAll(items.map { OrderItemEntity.from(it) }).map { it.toDomain() }
    }

    override fun findByOrderId(orderId: Long): List<OrderItem> {
        return orderItemJpaRepository.findByOrderId(orderId).map { it.toDomain() }
    }
}
