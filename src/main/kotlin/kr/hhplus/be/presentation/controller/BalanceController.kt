package kr.hhplus.be.presentation.controller

import jakarta.validation.Valid
import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.presentation.api.BalanceApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.BalanceChargeRequest
import kr.hhplus.be.presentation.dto.response.BalanceChargeResponse
import kr.hhplus.be.presentation.dto.response.BalanceQueryResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/balance")
class BalanceController(
    private val balanceService: BalanceService
) : BalanceApi {

    @PostMapping("/charge")
    override fun charge(
        @RequestBody @Valid request: BalanceChargeRequest
    ): BaseResponse<BalanceChargeResponse> {
        val result = balanceService.charge(BalanceChargeCommand(request.userId, request.amount))

        return BaseResponse.success(
            BalanceChargeResponse(
                userId = result.userId,
                balance = result.amount,
                chargedAmount = request.amount,
                chargedAt = java.time.LocalDateTime.now()
            )
        )
    }

    @GetMapping
    override fun getBalance(
        @RequestParam userId: Long
    ): BaseResponse<BalanceQueryResponse> {
        val balance = balanceService.getBalance(userId)

        return BaseResponse.success(
            BalanceQueryResponse(
                userId = userId,
                balance = balance.amount,
                lastUpdatedAt = balance.updatedAt
            )
        )
    }
}
