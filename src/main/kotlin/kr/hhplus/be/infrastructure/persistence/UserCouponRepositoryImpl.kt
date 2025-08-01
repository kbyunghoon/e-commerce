package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.user.UserCoupon
import kr.hhplus.be.domain.user.UserCouponRepository
import kr.hhplus.be.infrastructure.persistence.repository.UserCouponJpaRepository
import org.springframework.stereotype.Component

@Component
class UserCouponRepositoryImpl(
    private val userCouponJpaRepository: UserCouponJpaRepository
) : UserCouponRepository {
    override fun save(userCoupon: UserCoupon): UserCoupon {
        TODO("구현 예정")
    }

    override fun existsByUserIdAndCouponId(userId: Long, couponId: Long): Boolean {
        TODO("구현 예정")
    }

    override fun findByUserId(userId: Long): List<UserCoupon> {
        TODO("구현 예정")
    }

    override fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon? {
        TODO("구현 예정")
    }
}
