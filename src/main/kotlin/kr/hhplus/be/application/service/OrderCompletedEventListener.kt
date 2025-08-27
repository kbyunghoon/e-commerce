package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.events.OrderCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class OrderCompletedEventListener(
    private val orderNotificationService: OrderNotificationService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handleOrderCompleted(event: OrderCompletedEvent) {
        log.info("주문 완료 이벤트 수신 - orderId: {}, userId: {}", event.orderId, event.userId)
        
        try {
            orderNotificationService.sendOrderNotification(event)
            
            // TODO: 추가적인 후속 작업 (이메일 발송, SMS 전송, 통계 데이터 업데이트)

        } catch (e: Exception) {
            log.error("주문 완료 이벤트 처리 중 오류 발생 - orderId: {}", event.orderId, e)
        }
    }
}