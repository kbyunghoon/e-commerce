package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.OrderCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class OrderNotificationService {

    private val log = LoggerFactory.getLogger(javaClass)

    fun sendOrderNotification(event: OrderCompletedEvent) {
        try {
            log.info("주문 완료 알림 전송 시작 - orderId: {}, userId: {}", event.orderId, event.userId)

            sendToExternalSystem(event)

            log.info("주문 완료 알림 전송 완료 - orderId: {}", event.orderId)
        } catch (e: Exception) {
            log.error("주문 완료 알림 전송 실패 - orderId: {}", event.orderId, e)
        }
    }

    private fun sendToExternalSystem(event: OrderCompletedEvent) {}
}