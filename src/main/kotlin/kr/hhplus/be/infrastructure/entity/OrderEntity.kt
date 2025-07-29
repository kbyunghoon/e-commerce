package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "user_coupon_id")
    val userCouponId: Long?,

    @Column(name = "original_amount", nullable = false)
    val originalAmount: Int,

    @Column(name = "discount_amount", nullable = false)
    val discountAmount: Int,

    @Column(name = "final_amount", nullable = false)
    val finalAmount: Int,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: OrderStatus,

    @Column(name = "order_date")
    val orderDate: LocalDateTime?,

    @Column(name = "expires_at")
    val expiresAt: LocalDateTime? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Order {
        return Order(
            id = id,
            userId = userId,
            userCouponId = userCouponId,
            originalAmount = originalAmount,
            discountAmount = discountAmount,
            finalAmount = finalAmount,
            status = status,
            orderDate = orderDate,
            expireDate = expiresAt,
            createdAt = createdAt,
        )
    }

    companion object {
        fun from(order: Order): OrderEntity {
            return OrderEntity(
                id = order.id,
                userId = order.userId,
                userCouponId = order.userCouponId,
                originalAmount = order.originalAmount,
                discountAmount = order.discountAmount,
                finalAmount = order.finalAmount,
                status = order.status,
                orderDate = order.orderDate,
                expiresAt = order.expireDate,
                createdAt = order.createdAt,
            )
        }
    }
}