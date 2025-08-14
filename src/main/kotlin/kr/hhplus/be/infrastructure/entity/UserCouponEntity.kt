package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.user.UserCoupon
import java.time.LocalDateTime

@Entity
@Table(name = "user_coupons")
class UserCouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_coupon_id")
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "coupon_id", nullable = false)
    val couponId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    var status: CouponStatus,

    @Column(name = "issued_at", nullable = false)
    val issuedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "used_at")
    var usedAt: LocalDateTime? = null
) {
    fun toDomain(): UserCoupon {
        return UserCoupon(
            id = id,
            userId = userId,
            couponId = couponId,
            status = status,
            issuedAt = issuedAt,
            usedAt = usedAt,
        )
    }

    companion object {
        fun from(userCoupon: UserCoupon): UserCouponEntity {
            return UserCouponEntity(
                id = userCoupon.id,
                userId = userCoupon.userId,
                couponId = userCoupon.couponId,
                status = userCoupon.status,
                issuedAt = userCoupon.issuedAt,
                usedAt = userCoupon.usedAt
            )
        }
    }
}