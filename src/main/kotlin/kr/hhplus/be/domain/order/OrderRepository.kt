package kr.hhplus.be.domain.order

import org.springframework.stereotype.Repository

@Repository
interface OrderRepository {
    fun save(order: Order): Order
    fun findById(orderId: Long): Order?
}
