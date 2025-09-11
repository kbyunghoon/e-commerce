package kr.hhplus.be.application.event

import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.domain.CouponIssueEvent
import kr.hhplus.be.infrastructure.messaging.CouponIssueEventRequestKafkaPublisherImpl.Companion.COUPON_ISSUE_TOPIC
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class CouponIssueEventListener(
    private val couponService: CouponService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = [COUPON_ISSUE_TOPIC], groupId = "coupon_issue", concurrency = "3")
    fun handleCouponCompleted(event: CouponIssueEvent) {
        log.info("쿠폰 발급 처리 시작 - userId: {}, couponId: {}", event.userId, event.couponId)
        try {
            couponService.handleCouponIssueRequest(event.userId, event.couponId, event.issuedAt)
            log.info("쿠폰 발급 처리 완료 - userId: {}, couponId: {}", event.userId, event.couponId)

        } catch (e: Exception) {
            log.error("쿠폰 발급 처리 실패- userId: {}, couponId: {}", event.userId, event.couponId, e)
            throw e
        }
    }
}