package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.order.OrderStatus
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val orderId: Long = 0,

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

    @Column(name = "order_date", nullable = false)
    val orderDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)