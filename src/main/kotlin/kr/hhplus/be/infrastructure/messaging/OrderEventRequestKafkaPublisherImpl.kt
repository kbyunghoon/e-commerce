package kr.hhplus.be.infrastructure.messaging

import kr.hhplus.be.domain.order.OrderEventRequestKafkaPublisher
import kr.hhplus.be.domain.order.OrderCompletedEvent
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OrderEventRequestKafkaPublisherImpl(
    private val kafkaTemplate: KafkaTemplate<String, OrderCompletedEvent>,
) : OrderEventRequestKafkaPublisher {
    companion object {
        const val ORDER_COMPLETED_TOPIC = "order-completed"
    }

    override fun publish(event: OrderCompletedEvent) {
        kafkaTemplate.send(
            ORDER_COMPLETED_TOPIC, event
        )
    }
}