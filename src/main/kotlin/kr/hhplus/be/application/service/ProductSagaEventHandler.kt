package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.saga.events.StockDeductedEvent
import kr.hhplus.be.domain.order.saga.events.StockDeductionFailedEvent
import kr.hhplus.be.domain.order.saga.events.StockDeductionRequestedEvent
import kr.hhplus.be.domain.order.saga.events.StockRestoreRequestedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ProductSagaEventHandler(
    private val productService: ProductService,
    private val orderService: OrderService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleStockDeductionRequested(event: StockDeductionRequestedEvent) {
        try {
            log.info("재고 차감 요청 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)
            
            orderService.deductStockForSaga(event.orderId, event.userId)
            
            applicationEventPublisher.publishEvent(
                StockDeductedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId
                )
            )
        } catch (e: Exception) {
            log.error("재고 차감 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
            applicationEventPublisher.publishEvent(
                StockDeductionFailedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    reason = e.message ?: "재고 차감 실패"
                )
            )
        }
    }

    @EventListener
    @Async
    fun handleStockRestoreRequested(event: StockRestoreRequestedEvent) {
        try {
            log.info("재고 복구 요청 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)
            
            orderService.restoreStockForSaga(event.orderId, event.userId)
        } catch (e: Exception) {
            log.error("재고 복구 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
        }
    }
}