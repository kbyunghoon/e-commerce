package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.user.UserCoupon
import kr.hhplus.be.domain.user.UserCouponRepository
import kr.hhplus.be.infrastructure.entity.UserCouponEntity
import kr.hhplus.be.infrastructure.persistence.repository.UserCouponJpaRepository
import org.springframework.stereotype.Component

@Component
class UserCouponRepositoryImpl(
    private val userCouponJpaRepository: UserCouponJpaRepository
) : UserCouponRepository {
    override fun save(userCoupon: UserCoupon): UserCoupon {
        return userCouponJpaRepository.save(UserCouponEntity.from(userCoupon)).toDomain()
    }

    override fun existsByUserIdAndCouponId(userId: Long, couponId: Long): Boolean {
        return userCouponJpaRepository.existsByUserIdAndCouponId(userId, couponId)
    }

    override fun findByUserId(userId: Long): List<UserCoupon> {
        return userCouponJpaRepository.findByUserId(userId).map { it.toDomain() }
    }

    override fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon? {
        return userCouponJpaRepository.findByUserIdAndCouponId(userId, couponId)?.toDomain()
    }
}
