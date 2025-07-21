package kr.hhplus.be.application.port.`in`

import kr.hhplus.be.application.dto.CouponIssueCommand
import kr.hhplus.be.application.dto.CouponInfo

interface CouponUseCase {
    fun issue(command: CouponIssueCommand): CouponInfo
    fun getCoupons(userId: Long): List<CouponInfo>
}
