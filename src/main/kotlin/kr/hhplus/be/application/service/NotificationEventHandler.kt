package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.events.OrderCompletedEvent
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class NotificationEventHandler(
    private val orderNotificationService: OrderNotificationService,
//    private val emailService: EmailService,
//    private val smsService: SmsService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    @Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 1000))
    fun handleOrderCompletedForNotification(event: OrderCompletedEvent) {
        log.info("주문 완료 알림 처리 시작 - orderId: {}", event.orderId)
        
        try {
            // 외부 시스템 알림
            orderNotificationService.sendOrderNotification(event)
            
            // 이메일 발송 (비동기)
            // emailService.sendOrderConfirmationEmail(event)
            
            // SMS 발송 (비동기) 
            // smsService.sendOrderConfirmationSms(event)
            
            log.info("주문 완료 알림 처리 완료 - orderId: {}", event.orderId)
            
        } catch (e: Exception) {
            log.error("주문 완료 알림 처리 실패 - orderId: {}", event.orderId, e)
            throw e // Retry를 위해 예외 재발생
        }
    }
}