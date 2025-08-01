package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.coupon.Coupon
import java.time.LocalDateTime

@Entity
@Table(name = "coupons")
class CouponEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "code", nullable = false, unique = true)
    val code: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    val discountType: DiscountType,

    @Column(name = "discount_value", nullable = false)
    val discountValue: Int,

    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,

    @Column(name = "total_quantity", nullable = false)
    val totalQuantity: Int,

    @Column(name = "issued_quantity", nullable = false)
    var issuedQuantity: Int,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): Coupon {
        return Coupon(
            id = this.id,
            name = this.name,
            code = this.code,
            discountType = this.discountType,
            discountValue = this.discountValue,
            expiresAt = this.expiresAt,
            totalQuantity = this.totalQuantity,
            issuedQuantity = this.issuedQuantity,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    companion object {
        fun fromDomain(coupon: Coupon): CouponEntity {
            return CouponEntity(
                id = coupon.id,
                name = coupon.name,
                code = coupon.code,
                discountType = coupon.discountType,
                discountValue = coupon.discountValue,
                expiresAt = coupon.expiresAt,
                totalQuantity = coupon.totalQuantity,
                issuedQuantity = coupon.issuedQuantity,
                createdAt = coupon.createdAt,
                updatedAt = coupon.updatedAt
            )
        }
    }

    fun increaseIssuedQuantity() {
        if (this.issuedQuantity >= this.totalQuantity) {
            throw IllegalStateException("쿠폰 발급 수량이 모두 소진되었습니다.")
        }
        this.issuedQuantity++
        this.updatedAt = LocalDateTime.now()
    }
}