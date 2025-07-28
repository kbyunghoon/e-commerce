package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    val orderItemId: Long = 0,

    @Column(name = "order_id", nullable = false)
    val orderId: Long,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "quantity", nullable = false)
    val quantity: Int,

    @Column(name = "price_per_item", nullable = false)
    val pricePerItem: Int,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: OrderStatus,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)