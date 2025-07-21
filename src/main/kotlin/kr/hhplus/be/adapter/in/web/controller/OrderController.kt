package kr.hhplus.be.adapter.`in`.web.controller

import kr.hhplus.be.application.dto.OrderCreateCommand
import kr.hhplus.be.application.dto.OrderItemCreateCommand
import kr.hhplus.be.application.dto.PaymentProcessCommand
import kr.hhplus.be.application.port.`in`.OrderUseCase
import kr.hhplus.be.adapter.`in`.web.api.OrderApi
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.adapter.`in`.web.dto.request.OrderRequest
import kr.hhplus.be.adapter.`in`.web.dto.request.PaymentRequest
import kr.hhplus.be.adapter.`in`.web.dto.response.OrderItemResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.OrderResponse
import kr.hhplus.be.adapter.`in`.web.dto.response.PaymentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val orderUseCase: OrderUseCase) : OrderApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createOrder(@RequestBody request: OrderRequest): BaseResponse<OrderResponse> {
        val command = OrderCreateCommand.of(request)
        val orderInfo = orderUseCase.createOrder(command)

        val webOrderItems = orderInfo.orderItems.map { OrderItemResponse.from(it) }
        return BaseResponse.success(
            OrderResponse.from(orderInfo)
        )
    }

    @PostMapping("/{orderId}/pay")
    override fun pay(
        @PathVariable orderId: String,
        @RequestBody request: PaymentRequest
    ): BaseResponse<PaymentResponse> {
        val command = PaymentProcessCommand.of(orderId.toLong(), request)
        val orderInfo = orderUseCase.processPayment(command)

        return BaseResponse.success(
            PaymentResponse.from(orderInfo)
        )
    }

    @GetMapping("/{orderId}")
    fun getOrder(@PathVariable orderId: Long): BaseResponse<OrderResponse> {
        val orderInfo = orderUseCase.getOrder(orderId)

        val webOrderItems = orderInfo.orderItems.map { OrderItemResponse.from(it) }

        return BaseResponse.success(
            OrderResponse.from(orderInfo)
        )
    }
}
