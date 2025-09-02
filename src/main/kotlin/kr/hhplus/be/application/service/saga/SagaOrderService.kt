package kr.hhplus.be.application.service.saga

import kr.hhplus.be.domain.order.Order

interface SagaOrderService {

    fun getOrderForPayment(orderId: Long, userId: Long): Order

    fun deductStock(orderId: Long, userId: Long)

    fun restoreStock(orderId: Long, userId: Long)

    fun completeOrder(orderId: Long)

    fun cancelOrder(orderId: Long)

    fun getOrder(orderId: Long, userId: Long): Order
}