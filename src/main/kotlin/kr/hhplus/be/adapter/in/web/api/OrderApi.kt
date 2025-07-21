package kr.hhplus.be.adapter.`in`.web.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.request.OrderRequest
import kr.hhplus.be.adapter.`in`.web.dto.request.PaymentRequest
import kr.hhplus.be.adapter.`in`.web.dto.response.OrderResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.PaymentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus

@Tag(name = "주문 및 결제", description = "상품 주문 생성 및 결제 처리 API")
interface OrderApi {

    @Operation(
        summary = "주문 생성",
        description = "<h1>주문 생성 (결제 전)</h1><h2>주요 기능</h2>- 여러 상품을 담아 주문 생성<br/>- 쿠폰 적용하여 할인 가능<br/>- 재고 확인 후 주문 생성"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "주문 생성 성공"
            ),
            ApiResponse(
                responseCode = "400",
                description = "잘못된 요청 (재고 부족 등)"
            )
        ]
    )
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody request: OrderRequest): BaseResponse<OrderResponse>

    @Operation(
        summary = "결제 처리",
        description = "<h1>결제 처리</h1><h2>주요 기능</h2>- 생성된 주문에 대해 결제 처리<br/>- 잔액으로 결제하며, 부족 시 실패 처리<br/>- 결제 성공 시 잔액 차감, 재고 차감, 쿠폰 사용 원자적으로 처리"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "결제 성공"
            ),
            ApiResponse(
                responseCode = "402",
                description = "결제 실패 (잔액 부족)"
            )
        ]
    )
    fun pay(
        @Parameter(description = "주문 생성 시 발급된 임시 주문 ID", required = true)
        @PathVariable orderId: String,
        @RequestBody request: PaymentRequest
    ): BaseResponse<PaymentResponse>
}