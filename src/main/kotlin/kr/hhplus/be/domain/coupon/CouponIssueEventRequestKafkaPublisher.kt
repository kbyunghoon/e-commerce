package kr.hhplus.be.domain.coupon

import kr.hhplus.be.domain.CouponIssueEvent

interface CouponIssueEventRequestKafkaPublisher {
    fun publish(event: CouponIssueEvent)
}