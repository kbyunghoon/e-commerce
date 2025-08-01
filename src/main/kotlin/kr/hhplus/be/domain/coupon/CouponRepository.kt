package kr.hhplus.be.domain.coupon

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode

interface CouponRepository {
    fun findById(id: Long): Coupon?
    fun save(coupon: Coupon): Coupon
    
    fun findByIdOrThrow(id: Long): Coupon {
        return findById(id) ?: throw BusinessException(ErrorCode.COUPON_NOT_FOUND)
    }
}
