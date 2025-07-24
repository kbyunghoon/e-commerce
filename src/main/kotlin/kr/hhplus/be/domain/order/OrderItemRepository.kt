package kr.hhplus.be.domain.order

interface OrderItemRepository {
    fun saveAll(items: List<OrderItem>): List<OrderItem>
    fun findByOrderId(orderId: Long): List<OrderItem>
}
