package kr.hhplus.be.application.service

import kr.hhplus.be.application.dto.CouponInfo
import kr.hhplus.be.application.dto.CouponIssueCommand
import kr.hhplus.be.application.port.`in`.CouponUseCase
import kr.hhplus.be.domain.enums.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.stereotype.Service

@Service
class CouponService : CouponUseCase {
    override fun issue(command: CouponIssueCommand): CouponInfo {
        if (command.name == "SOLD_OUT_COUPON") {
            throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
        }

        return CouponInfo(
            id = 1L,
            name = command.name,
            discountType = command.discountType,
            discountValue = command.discountValue,
            status = CouponStatus.AVAILABLE,
            expiredAt = command.expiredAt
        )
    }

    override fun getCoupons(userId: Long): List<CouponInfo> {
        // TODO: 추후 DB 조회 로직 추가 예정
        return listOf(
            CouponInfo(
                id = 1L,
                name = "10% 할인 쿠폰",
                discountType = kr.hhplus.be.domain.enums.DiscountType.PERCENTAGE,
                discountValue = 10,
                status = CouponStatus.AVAILABLE,
                expiredAt = java.time.LocalDate.now().plusDays(30)
            )
        )
    }
}
