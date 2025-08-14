package kr.hhplus.be.domain.coupon

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode

interface CouponRepository {
    fun findById(couponId: Long): Coupon?
    fun save(coupon: Coupon): Coupon
    fun findByIdWithPessimisticLock(couponId: Long): Coupon
    
    fun findByIdOrThrow(couponId: Long): Coupon {
        return findById(couponId) ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)
    }
}
