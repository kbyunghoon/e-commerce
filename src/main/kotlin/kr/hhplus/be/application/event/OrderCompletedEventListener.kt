package kr.hhplus.be.application.event

import kr.hhplus.be.application.service.OrderNotificationService
import kr.hhplus.be.domain.order.OrderCompletedEvent
import kr.hhplus.be.infrastructure.messaging.OrderEventRequestKafkaPublisherImpl.Companion.ORDER_COMPLETED_TOPIC
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class OrderCompletedEventListener(
    private val orderNotificationService: OrderNotificationService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [ORDER_COMPLETED_TOPIC], groupId = "order_completed", concurrency = "3")
    fun handleOrderCompleted(event: OrderCompletedEvent) {
        log.info("주문 완료 알림 처리 시작 - orderId: {}", event.orderId)
        try {
            orderNotificationService.sendOrderNotification(event)
            log.info("주문 완료 알림 처리 완료 - orderId: {}", event.orderId)

        } catch (e: Exception) {
            log.error("주문 완료 알림 처리 실패 - orderId: {}", event.orderId, e)
            throw e
        }
    }
}