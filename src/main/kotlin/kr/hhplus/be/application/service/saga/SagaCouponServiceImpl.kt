package kr.hhplus.be.application.service.saga

import kr.hhplus.be.application.service.CouponService
import org.springframework.stereotype.Service

@Service
class SagaCouponServiceImpl(
    private val couponService: CouponService
) : SagaCouponService {

    override fun useCoupon(userId: Long, couponId: Long) {
        couponService.useCouponForSaga(userId, couponId)
    }

    override fun restoreCoupon(userId: Long, couponId: Long) {
        couponService.restoreCouponForSaga(userId, couponId)
    }
}