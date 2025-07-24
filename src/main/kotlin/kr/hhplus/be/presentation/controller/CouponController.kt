package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.presentation.api.CouponApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.CouponIssueRequest
import kr.hhplus.be.presentation.dto.response.CouponIssueResponse
import kr.hhplus.be.presentation.dto.response.CouponListResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController(
    private val couponService: CouponService
) : CouponApi {

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    override fun issueCoupon(@RequestBody request: CouponIssueRequest): BaseResponse<CouponIssueResponse> {
        val coupon = couponService.issue(CouponIssueCommand.of(request))
        
        return BaseResponse.success(CouponIssueResponse.from(coupon))
    }

    @GetMapping
    override fun getCoupons(
        @RequestParam userId: Long,
        @RequestParam status: String?
    ): BaseResponse<CouponListResponse> {
        val userCoupons = couponService.getUserCoupons(userId)
        
        return BaseResponse.success(CouponListResponse.from(userCoupons))
    }
}
