package kr.hhplus.be.presentation.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.domain.enums.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.presentation.dto.request.CouponIssueRequest
import kr.hhplus.be.presentation.dto.response.CouponIssueResponse
import kr.hhplus.be.presentation.dto.response.CouponListResponse
import kr.hhplus.be.presentation.dto.response.CouponResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@Tag(name = "쿠폰 시스템", description = "선착순 쿠폰 발급 및 보유 쿠폰 조회 API")
@RestController
@RequestMapping("/api/v1/coupons")
class CouponController(private val couponService: CouponService) {

    @Operation(
        summary = "선착순 쿠폰 발급",
        description = "<h1>선착순 쿠폰 발급</h1><h2>주요 기능</h2>- 선착순으로 쿠폰 발급<br/>- 정해진 수량만큼만 발급<br/>- 한 사용자는 동일한 쿠폰을 중복 발급 불가능<br/>- 동시 요청 시에도 정해진 수량 이상 발급되지 않도록 제어"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "쿠폰 발급 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "쿠폰 발급 실패 (수량 소진)"
            ),
            ApiResponse(
                responseCode = "409",
                description = "중복 발급 시도"
            )
        ]
    )
    @PostMapping("/issue")
    @ResponseStatus(HttpStatus.CREATED)
    fun issueCoupon(@RequestBody request: CouponIssueRequest): BaseResponse<CouponIssueResponse> {
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

    @Operation(
        summary = "보유 쿠폰 조회",
        description = "<h1>보유 쿠폰 목록 조회</h1><h2>주요 기능</h2>- 사용자가 보유한 쿠폰 목록 조회<br/>- 쿠폰 상태(사용 가능, 사용 완료, 만료)별로 필터링<br/>- 발급일 기준 내림차순으로 정렬"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "조회 성공"
            )
        ]
    )
    @GetMapping
    fun getCoupons(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @RequestParam userId: Long,
        @Parameter(description = "쿠폰 상태 필터", required = false, example = "AVAILABLE")
        @RequestParam status: String? = null
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
