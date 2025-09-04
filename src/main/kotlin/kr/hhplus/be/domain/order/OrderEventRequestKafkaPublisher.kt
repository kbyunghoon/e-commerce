package kr.hhplus.be.domain.order

interface OrderEventRequestKafkaPublisher {
    fun publish(event: OrderCompletedEvent)
}