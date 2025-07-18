package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.domain.enums.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.presentation.api.CouponApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.presentation.dto.request.CouponIssueRequest
import kr.hhplus.be.presentation.dto.response.CouponIssueResponse
import kr.hhplus.be.presentation.dto.response.CouponListResponse
import kr.hhplus.be.presentation.dto.response.CouponResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController(private val couponService: CouponService) : CouponApi {

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    override fun issueCoupon(@RequestBody request: CouponIssueRequest): BaseResponse<CouponIssueResponse> {
        val success = couponService.issueCoupon(request.userId, request.couponId)
        return if (success) {
            BaseResponse.success(
                CouponIssueResponse(
                    userCouponId = 1,
                    userId = request.userId,
                    couponId = request.couponId,
                    couponName = "10% 할인 쿠폰",
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = 10,
                    expiresAt = LocalDateTime.now().plusDays(30),
                    issuedAt = LocalDateTime.now()
                )
            )
        } else {
            throw BusinessException(ErrorCode.COUPON_ISSUE_FAILED)
        }
    }

    @GetMapping
    override fun getCoupons(
        @RequestParam userId: Long,
        @RequestParam status: String?
    ): BaseResponse<CouponListResponse> {
        val coupons = couponService.getCoupons(userId, status)
        val webCoupons = coupons.map { couponInfo ->
            CouponResponse(
                userCouponId = couponInfo.userCouponId,
                couponId = couponInfo.couponId,
                couponName = couponInfo.couponName,
                discountType = couponInfo.discountType,
                discountValue = couponInfo.discountValue,
                status = couponInfo.status,
                expiryDate = couponInfo.expiryDate,
                issuedAt = couponInfo.issuedAt
            )
        }
        return BaseResponse.success(
            CouponListResponse(
                coupons = webCoupons
            )
        )
    }
}
