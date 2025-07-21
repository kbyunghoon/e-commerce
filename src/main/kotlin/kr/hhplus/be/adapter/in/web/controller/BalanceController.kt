package kr.hhplus.be.adapter.`in`.web.controller

import kr.hhplus.be.application.dto.BalanceChargeCommand
import kr.hhplus.be.application.port.`in`.BalanceUseCase
import kr.hhplus.be.adapter.`in`.web.api.BalanceApi
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.request.BalanceChargeRequest
import kr.hhplus.be.adapter.`in`.web.dto.response.BalanceChargeResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.BalanceQueryResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/balance")
class BalanceController(
    private val balanceUseCase: BalanceUseCase
) : BalanceApi {

    @PostMapping("/charge")
    override fun charge(
        @RequestBody request: BalanceChargeRequest
    ): BaseResponse<BalanceChargeResponse> {
        val command = BalanceChargeCommand.of(request)

        val balanceInfo = balanceUseCase.charge(command)

        val response = BalanceChargeResponse.from(balanceInfo, request.amount)

        return BaseResponse.success(response)
    }

    @GetMapping
    override fun getBalance(
        @RequestParam userId: Long
    ): BaseResponse<BalanceQueryResponse> {
        val balanceInfo = balanceUseCase.getBalance(userId)

        val response = BalanceQueryResponse.from(balanceInfo)

        return BaseResponse.success(response)
    }
}
