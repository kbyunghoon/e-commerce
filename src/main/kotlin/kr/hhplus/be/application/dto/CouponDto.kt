package kr.hhplus.be.application.dto

import kr.hhplus.be.adapter.`in`.web.dto.request.CouponIssueRequest
import kr.hhplus.be.domain.enums.CouponStatus
import kr.hhplus.be.domain.enums.DiscountType
import java.time.LocalDate

data class CouponIssueCommand(
    val userId: Long,
    val name: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val issueCount: Int,
    val expiredAt: LocalDate,
) {
    companion object {
        fun of(request: CouponIssueRequest): CouponIssueCommand {
            return CouponIssueCommand(
                userId = request.userId,
                name = request.name,
                discountType = request.discountType,
                discountValue = request.discountValue,
                issueCount = request.issueCount,
                expiredAt = request.expiredAt
            )
        }
    }
}

data class CouponInfo(
    val id: Long,
    val name: String,
    val discountType: DiscountType,
    val discountValue: Int,
    val status: CouponStatus,
    val expiredAt: LocalDate,
)
