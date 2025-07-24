package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.facade.OrderFacade
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
    private val orderFacade: OrderFacade
) : OrderApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createOrder(@RequestBody request: OrderRequest): BaseResponse<OrderResponse> {
        val orderResponse = orderFacade.processOrder(OrderCreateCommand.of(request))

        return BaseResponse.success(orderResponse)
    }

    @PostMapping("/{orderId}/pay")
    override fun pay(
        @PathVariable orderId: String,
        @RequestBody request: PaymentRequest
    ): BaseResponse<PaymentResponse> {
        val orderResponse = orderFacade.processPayment(PaymentProcessCommand.of(request))

        return BaseResponse.success(
            PaymentResponse.from(orderResponse)
        )
    }

    @GetMapping("/{orderId}")
    fun getOrder(
        @PathVariable orderId: Long,
        @RequestParam userId: Long
    ): BaseResponse<OrderResponse> {
        val orderResponse = orderFacade.getOrder(userId, orderId)

        return BaseResponse.success(orderResponse)
    }
}
