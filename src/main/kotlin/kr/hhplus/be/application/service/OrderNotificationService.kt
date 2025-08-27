package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.events.OrderCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class OrderNotificationService {

    private var restTemplate: RestTemplate = RestTemplate()
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

    private fun sendToExternalSystem(event: OrderCompletedEvent) {
        val payload = mapOf(
            "orderId" to event.orderId,
            "userId" to event.userId,
            "totalAmount" to event.totalAmount,
            "finalAmount" to event.finalAmount,
            "discountAmount" to event.discountAmount,
            "items" to event.orderItems.map { item ->
                mapOf(
                    "productId" to item.productId,
                    "productName" to item.productName,
                    "quantity" to item.quantity,
                    "pricePerItem" to item.pricePerItem
                )
            },
            "completedAt" to event.completedAt
        )

        log.debug("외부 시스템 전송 데이터: {}", payload)

        restTemplate.postForObject("http://external-api/orders", payload, String::class.java)
    }
}