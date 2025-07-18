package kr.hhplus.be.presentation.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.BalanceChargeRequest
import kr.hhplus.be.presentation.dto.response.BalanceChargeResponse
import kr.hhplus.be.presentation.dto.response.BalanceQueryResponse
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "잔액 관리", description = "사용자 잔액 충전 및 조회 API")
interface BalanceApi {

    @Operation(
        summary = "잔액 충전",
        description = "<h1>사용자 잔액 충전</h1><h2>주요 기능</h2>- 사용자 잔액 충전<br/>- 잔고가 없으면 새로 생성, 있으면 기존 금액에 추가"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "충전 성공"),
            ApiResponse(responseCode = "400", description = "잘못된 요청 (충전 금액이 0 이하)")
        ]
    )
    fun charge(@RequestBody request: BalanceChargeRequest): BaseResponse<BalanceChargeResponse>

    @Operation(
        summary = "잔액 조회",
        description = "<h1>사용자 잔액 조회</h1><h2>주요 기능</h2>- 사용자 현재 잔액 조회<br/>- 잔액이 없는 경우 0원으로 표시"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "조회 성공")
        ]
    )
    fun getBalance(
        @Parameter(description = "사용자 ID", required = true, example = "1") @RequestParam userId: Long
    ): BaseResponse<BalanceQueryResponse>
}