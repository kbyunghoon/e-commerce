package kr.hhplus.be.application.service.saga

import kr.hhplus.be.application.service.OrderService
import org.springframework.stereotype.Service

@Service  
class SagaProductServiceImpl(
    private val orderService: OrderService
) : SagaProductService {

    override fun deductStock(orderId: Long, userId: Long) {
        orderService.deductStockForSaga(orderId, userId)
    }

    override fun restoreStock(orderId: Long, userId: Long) {
        orderService.restoreStockForSaga(orderId, userId)
    }
}