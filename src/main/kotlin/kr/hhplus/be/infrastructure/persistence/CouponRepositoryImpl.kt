package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.infrastructure.entity.CouponEntity
import kr.hhplus.be.infrastructure.persistence.repository.CouponJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class CouponRepositoryImpl(
    private val couponJpaRepository: CouponJpaRepository
) : CouponRepository {
    override fun findById(couponId: Long): Coupon? {
        return couponJpaRepository.findByIdOrNull(couponId)?.toDomain()
    }

    override fun save(coupon: Coupon): Coupon {
        return couponJpaRepository.save(CouponEntity.from(coupon)).toDomain()
    }

    override fun findByIdWithPessimisticLock(couponId: Long): Coupon {
        return couponJpaRepository.findByIdWithPessimisticLock(couponId)?.toDomain() ?: throw BusinessException(
            ErrorCode.COUPON_NOT_FOUND
        )
    }
}
