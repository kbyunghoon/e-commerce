package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderRepository
import kr.hhplus.be.infrastructure.entity.OrderEntity
import kr.hhplus.be.infrastructure.persistence.repository.OrderJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order {
        return orderJpaRepository.save(OrderEntity.from(order)).toDomain()
    }

    override fun findById(orderId: Long): Order? {
        return orderJpaRepository.findByIdOrNull(orderId)?.toDomain()
    }
}
