package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    val id: Long? = 0,

    @Column(name = "order_id", nullable = false)
    val orderId: Long,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "product_name", nullable = false)
    val productName: String,

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
) {
    fun toDomain(): OrderItem {
        return OrderItem(
            id = id,
            orderId = orderId,
            productId = productId,
            productName = productName,
            quantity = quantity,
            pricePerItem = pricePerItem,
            status = status,
        )
    }

    companion object {
        fun from(orderItem: OrderItem): OrderItemEntity {
            return OrderItemEntity(
                id = orderItem.id,
                orderId = orderItem.orderId!!,
                productId = orderItem.productId,
                productName = orderItem.productName,
                quantity = orderItem.quantity,
                pricePerItem = orderItem.pricePerItem,
                status = orderItem.status,
            )
        }
    }
}