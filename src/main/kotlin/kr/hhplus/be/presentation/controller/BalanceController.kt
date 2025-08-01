package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.presentation.api.BalanceApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.presentation.dto.request.BalanceChargeRequest
import kr.hhplus.be.presentation.dto.response.BalanceChargeResponse
import kr.hhplus.be.presentation.dto.response.BalanceQueryResponse
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/v1/balance")
class BalanceController(
    private val balanceService: BalanceService
) : BalanceApi {

    @PostMapping("/charge")
    override fun charge(
        @RequestBody request: BalanceChargeRequest
    ): BaseResponse<BalanceChargeResponse> {
        val success = balanceService.charge(request.userId, request.amount)
        return if (success) {
            BaseResponse.success(
                BalanceChargeResponse(
                    userId = request.userId,
                    balance = 20000,
                    chargedAmount = request.amount,
                    chargedAt = LocalDateTime.now()
                )
            )
        } else {
            throw BusinessException(ErrorCode.CHARGE_FAILED)
        }
    }

    @GetMapping
    override fun getBalance(
        @RequestParam userId: Long
    ): BaseResponse<BalanceQueryResponse> {
        val balance = balanceService.getBalance(userId)
        return BaseResponse.success(
            BalanceQueryResponse(
                userId = userId,
                balance = balance,
                lastUpdatedAt = LocalDateTime.now()
            )
        )
    }
}
