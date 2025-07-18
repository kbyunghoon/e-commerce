package kr.hhplus.be.presentation.controller

import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.presentation.api.OrderApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.request.OrderRequest
import kr.hhplus.be.presentation.dto.request.PaymentRequest
import kr.hhplus.be.presentation.dto.response.OrderItemResponse
import kr.hhplus.be.presentation.dto.response.OrderResponse
import kr.hhplus.be.presentation.dto.response.PaymentResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/orders")
class OrderController(private val orderService: OrderService) : OrderApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    override fun createOrder(@RequestBody request: OrderRequest): BaseResponse<OrderResponse> {
        val orderResult = orderService.createOrder(
            request.userId,
            request.items.map {
                kr.hhplus.be.application.dto.OrderItemRequest(
                    it.productId,
                    it.quantity
                )
            },
            request.couponId
        )
        val webOrderItems = orderResult.items.map { orderItem ->
            OrderItemResponse(
                productId = orderItem.productId,
                productName = orderItem.productName,
                price = orderItem.price,
                quantity = orderItem.quantity
            )
        }
        return BaseResponse.success(
            OrderResponse(
                orderId = orderResult.orderId,
                userId = orderResult.userId,
                items = webOrderItems,
                originalAmount = orderResult.originalAmount,
                discountAmount = orderResult.discountAmount,
                finalAmount = orderResult.finalAmount,
                status = orderResult.status
            )
        )
    }

    @PostMapping("/{orderId}/pay")
    override fun pay(
        @PathVariable orderId: String,
        @RequestBody request: PaymentRequest
    ): BaseResponse<PaymentResponse> {
        val paymentResult = orderService.pay(orderId, request.userId, request.paymentMethod)
        return BaseResponse.success(
            PaymentResponse(
                orderId = paymentResult.orderId,
                orderNumber = paymentResult.orderNumber,
                userId = paymentResult.userId,
                finalAmount = paymentResult.finalAmount,
                status = paymentResult.status,
                orderedAt = paymentResult.orderedAt
            )
        )
    }
}
