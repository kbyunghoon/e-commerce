package kr.hhplus.be.infrastructure.persistence.repository.jpa

import kr.hhplus.be.domain.user.UserCouponDetail
import kr.hhplus.be.infrastructure.entity.UserCouponEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserCouponJpaRepository : JpaRepository<UserCouponEntity, Long> {
    fun existsByUserIdAndCouponId(userId: Long, couponId: Long): Boolean
    fun findByUserId(userId: Long): List<UserCouponEntity>
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCouponEntity?
    fun findByCouponId(couponId: Long): List<UserCouponEntity>

    @Query("""
        SELECT new kr.hhplus.be.domain.user.UserCouponDetail(
            uc.id,
            uc.userId,
            uc.couponId,
            c.name,
            c.discountType,
            c.discountValue,
            uc.status,
            c.expiresAt,
            uc.issuedAt,
            uc.usedAt
        )
        FROM UserCouponEntity uc
        INNER JOIN CouponEntity c ON uc.couponId = c.id
        WHERE uc.userId = :userId
    """)
    fun findUserCouponDetails(userId: Long): List<UserCouponDetail>
}