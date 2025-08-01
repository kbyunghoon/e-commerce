package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderItemRepository
import kr.hhplus.be.infrastructure.persistence.repository.OrderItemJpaRepository
import org.springframework.stereotype.Component

@Component
class OrderItemRepositoryImpl(
    private val orderItemJpaRepository: OrderItemJpaRepository
) : OrderItemRepository {
    override fun saveAll(items: List<OrderItem>): List<OrderItem> {
        TODO("구현 예정")
    }

    override fun findByOrderId(orderId: Long): List<OrderItem> {
        TODO("구현 예정")
    }
}
