package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.infrastructure.persistence.repository.CouponJpaRepository
import org.springframework.stereotype.Component

@Component
class CouponRepositoryImpl(
    private val couponJpaRepository: CouponJpaRepository
) : CouponRepository {
    override fun findById(id: Long): Coupon? {
        TODO("구현 예정")
    }

    override fun save(coupon: Coupon): Coupon {
        TODO("구현 예정")
    }
}
