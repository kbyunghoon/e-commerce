package kr.hhplus.be.presentation.controller

import jakarta.validation.Valid
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.application.service.PaymentSagaOrchestrator
import kr.hhplus.be.presentation.api.OrderApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.OrderRequest
import kr.hhplus.be.presentation.dto.request.PaymentRequest
import kr.hhplus.be.presentation.dto.response.OrderResponse
import kr.hhplus.be.presentation.dto.response.PaymentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(
    private val orderService: OrderService,
    private val paymentSagaOrchestrator: PaymentSagaOrchestrator
) : OrderApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createOrder(@RequestBody @Valid request: OrderRequest): BaseResponse<OrderResponse> {
        val orderData = orderService.processOrder(request.toCommand())
        val response = OrderResponse.from(orderData)

        return BaseResponse.success(response)
    }

    @PostMapping("/{orderId}/pay")
    override fun pay(
        @RequestBody @Valid request: PaymentRequest
    ): BaseResponse<PaymentResponse> {
        val orderData = paymentSagaOrchestrator.executePaymentSaga(request.toCommand())

        return BaseResponse.success(
            PaymentResponse.from(orderData)
        )
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @PathVariable orderId: Long,
        @RequestParam userId: Long
    ): BaseResponse<OrderResponse> {
        val orderData = orderService.getOrder(orderId, userId)
        val response = OrderResponse.from(orderData)

        return BaseResponse.success(response)
    }
}