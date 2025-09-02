package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.saga.events.OrderCancellationRequestedEvent
import kr.hhplus.be.domain.order.saga.events.OrderCompletedEvent
import kr.hhplus.be.domain.order.saga.events.OrderCompletionFailedEvent
import kr.hhplus.be.domain.order.saga.events.OrderCompletionRequestedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class OrderSagaEventHandler(
    private val orderService: OrderService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleOrderCompletionRequested(event: OrderCompletionRequestedEvent) {
        try {
            log.info("주문 완료 요청 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)
            
            orderService.completeOrderForSaga(event.orderId)
            
            applicationEventPublisher.publishEvent(
                OrderCompletedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId
                )
            )
        } catch (e: Exception) {
            log.error("주문 완료 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
            applicationEventPublisher.publishEvent(
                OrderCompletionFailedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    reason = e.message ?: "주문 완료 실패"
                )
            )
        }
    }

    @EventListener
    @Async
    fun handleOrderCancellationRequested(event: OrderCancellationRequestedEvent) {
        try {
            log.info("주문 취소 요청 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)
            
            orderService.cancelOrderForSaga(event.orderId)
        } catch (e: Exception) {
            log.error("주문 취소 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
        }
    }
}