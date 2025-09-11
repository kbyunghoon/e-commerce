package kr.hhplus.be.global.config

import org.apache.kafka.clients.admin.NewTopic
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder

@Configuration
class KafkaConfig {
    @Bean
    fun orderCompletedTopic(): NewTopic {
        return TopicBuilder
            .name("order-completed")
            .replicas(3)
            .partitions(3)
            .build()
    }

    @Bean
    fun issueCouponTopic(): NewTopic {
        return TopicBuilder
            .name("coupon-issue")
            .replicas(3)
            .partitions(3)
            .build()
    }
}