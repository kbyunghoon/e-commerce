package kr.hhplus.be.presentation.controller

import jakarta.validation.Valid
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.presentation.api.CouponApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.CouponIssueRequest
import kr.hhplus.be.presentation.dto.response.UserCouponListResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController(
    private val couponService: CouponService
) : CouponApi {

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    override fun issueCoupon(@RequestBody @Valid request: CouponIssueRequest): BaseResponse<Unit> {
        couponService.issue(request.toCommand())

        return BaseResponse.success()
    }

    @GetMapping
    override fun getCoupons(
        @RequestParam userId: Long,
        @RequestParam status: String?
    ): BaseResponse<UserCouponListResponse> {
        val userCoupons = couponService.getUserCoupons(userId)

        return BaseResponse.success(UserCouponListResponse.from(userCoupons))
    }
}
