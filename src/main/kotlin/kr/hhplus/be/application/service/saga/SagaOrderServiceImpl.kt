package kr.hhplus.be.application.service.saga

import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.domain.order.Order
import org.springframework.stereotype.Service

/**
 * 현재 모놀리식 환경에서의 SagaOrderService 구현체
 * MSA 전환 시 이 구현체를 REST Client 또는 Message Client로 교체
 */
@Service
class SagaOrderServiceImpl(
    private val orderService: OrderService
) : SagaOrderService {

    override fun getOrderForPayment(orderId: Long, userId: Long): Order {
        return orderService.getOrderForPayment(orderId, userId)
    }

    override fun deductStock(orderId: Long, userId: Long) {
        orderService.deductStockForSaga(orderId, userId)
    }

    override fun restoreStock(orderId: Long, userId: Long) {
        orderService.restoreStockForSaga(orderId, userId)
    }

    override fun completeOrder(orderId: Long) {
        orderService.completeOrderForSaga(orderId)
    }

    override fun cancelOrder(orderId: Long) {
        orderService.cancelOrderForSaga(orderId)
    }

    override fun getOrder(orderId: Long, userId: Long): Order {
        return orderService.getOrder(orderId, userId)
    }
}