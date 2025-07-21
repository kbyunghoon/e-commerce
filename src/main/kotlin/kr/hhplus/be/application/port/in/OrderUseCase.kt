package kr.hhplus.be.application.port.`in`

import kr.hhplus.be.application.dto.OrderCreateCommand
import kr.hhplus.be.application.dto.OrderInfo
import kr.hhplus.be.application.dto.PaymentProcessCommand

interface OrderUseCase {
    fun createOrder(command: OrderCreateCommand): OrderInfo
    fun processPayment(command: PaymentProcessCommand): OrderInfo
    fun getOrder(orderId: Long): OrderInfo
}
