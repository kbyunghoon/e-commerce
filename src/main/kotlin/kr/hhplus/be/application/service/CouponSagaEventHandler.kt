package kr.hhplus.be.application.service

import kr.hhplus.be.domain.order.saga.events.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class CouponSagaEventHandler(
    private val couponService: CouponService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleCouponUsageRequested(event: CouponUsageRequestedEvent) {
        try {
            log.info("쿠폰 사용 요청 - sagaId: {}, userId: {}, couponId: {}", 
                event.sagaId, event.userId, event.couponId)
            
            couponService.use(event.userId, event.couponId)
            
            applicationEventPublisher.publishEvent(
                CouponUsedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    userId = event.userId,
                    couponId = event.couponId
                )
            )
        } catch (e: Exception) {
            log.error("쿠폰 사용 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
            applicationEventPublisher.publishEvent(
                CouponUsageFailedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    userId = event.userId,
                    couponId = event.couponId,
                    reason = e.message ?: "Coupon usage failed"
                )
            )
        }
    }

    @EventListener
    @Async
    fun handleCouponRestoreRequested(event: CouponRestoreRequestedEvent) {
        try {
            log.info("쿠폰 복구 요청 - sagaId: {}, userId: {}, couponId: {}", 
                event.sagaId, event.userId, event.couponId)
            
            couponService.restore(event.userId, event.couponId)
        } catch (e: Exception) {
            log.error("쿠폰 복구 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
        }
    }
}