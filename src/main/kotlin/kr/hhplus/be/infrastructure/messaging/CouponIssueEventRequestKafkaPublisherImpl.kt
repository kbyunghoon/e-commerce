package kr.hhplus.be.infrastructure.messaging

import kr.hhplus.be.domain.CouponIssueEvent
import kr.hhplus.be.domain.coupon.CouponIssueEventRequestKafkaPublisher
import kr.hhplus.be.domain.order.OrderEventRequestKafkaPublisher
import kr.hhplus.be.domain.order.OrderCompletedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class CouponIssueEventRequestKafkaPublisherImpl(
    private val kafkaTemplate: KafkaTemplate<String, CouponIssueEvent>,
) : CouponIssueEventRequestKafkaPublisher {
    companion object {
        const val COUPON_ISSUE_TOPIC = "coupon-issue"
    }

    override fun publish(event: CouponIssueEvent) {
        kafkaTemplate.send(
            COUPON_ISSUE_TOPIC, event
        )
    }
}