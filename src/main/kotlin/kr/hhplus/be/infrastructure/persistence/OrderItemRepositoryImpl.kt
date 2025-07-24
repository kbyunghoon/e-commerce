package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.order.OrderItemRepository
import kr.hhplus.be.infrastructure.persistence.repository.OrderItemJpaRepository
import org.springframework.stereotype.Component

@Component
class OrderItemRepositoryImpl(
    private val orderItemJpaRepository: OrderItemJpaRepository
) : OrderItemRepository {
}
