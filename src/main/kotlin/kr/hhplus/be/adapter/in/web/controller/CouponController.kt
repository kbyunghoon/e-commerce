package kr.hhplus.be.adapter.`in`.web.controller

import kr.hhplus.be.application.dto.CouponIssueCommand
import kr.hhplus.be.application.port.`in`.CouponUseCase
import kr.hhplus.be.adapter.`in`.web.api.CouponApi
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.request.CouponIssueRequest
import kr.hhplus.be.adapter.`in`.web.dto.response.CouponIssueResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.CouponListResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.CouponResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/coupons")
class CouponController(private val couponUseCase: CouponUseCase) : CouponApi {

    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    override fun issueCoupon(@RequestBody request: CouponIssueRequest): BaseResponse<CouponIssueResponse> {
        val command = CouponIssueCommand.of(request)

        val couponInfo = couponUseCase.issue(command)

        val response = CouponIssueResponse.from(couponInfo, request.userId)

        return BaseResponse.success(response)
    }

    @GetMapping
    override fun getCoupons(
        @RequestParam userId: Long,
        @RequestParam status: String?
    ): BaseResponse<CouponListResponse> {
        val couponInfos = couponUseCase.getCoupons(userId)
        val webCoupons = couponInfos.map { couponInfo ->
            CouponResponse.from(couponInfo)
        }
        return BaseResponse.success(
            CouponListResponse(
                coupons = webCoupons
            )
        )
    }
}
