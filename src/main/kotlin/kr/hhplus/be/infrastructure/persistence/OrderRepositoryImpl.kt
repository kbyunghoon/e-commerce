package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderRepository
import kr.hhplus.be.infrastructure.persistence.repository.OrderJpaRepository
import org.springframework.stereotype.Component

@Component
class OrderRepositoryImpl(
    private val orderJpaRepository: OrderJpaRepository
) : OrderRepository {
    override fun save(order: Order): Order {
        TODO("구현 예정")
    }

    override fun findById(orderId: Long): Order? {
        TODO("구현 예정")
    }
}
