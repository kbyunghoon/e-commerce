package kr.hhplus.be.application.service.saga

interface SagaCouponService {

    fun useCoupon(userId: Long, couponId: Long)

    fun restoreCoupon(userId: Long, couponId: Long)
}