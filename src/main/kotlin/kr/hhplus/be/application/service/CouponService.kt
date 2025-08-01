package kr.hhplus.be.application.service

import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.domain.enums.CouponStatus
import kr.hhplus.be.domain.enums.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.model.Coupon
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CouponService {
    fun issueCoupon(userId: Long, couponId: Long): Boolean {
        if (couponId == 999L) {
            throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
        }
        return true
    }

    fun getCoupons(userId: Long, status: String?): List<Coupon> {
        val coupon = Coupon(
            userCouponId = 1,
            couponId = 1,
            couponName = "10% 할인 쿠폰",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            status = CouponStatus.AVAILABLE,
            expiryDate = LocalDateTime.now().plusDays(30),
            issuedAt = LocalDateTime.now()
        )
        return listOf(coupon)
    }
}
