package kr.hhplus.be.infrastructure.persistence

import com.fasterxml.jackson.databind.ObjectMapper
import kr.hhplus.be.domain.order.saga.PaymentSaga
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import java.time.Duration

@Repository
class PaymentSagaRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) : PaymentSagaRepository {

    companion object {
        private const val SAGA_KEY_PREFIX = "payment:saga:"
        private const val ORDER_SAGA_KEY_PREFIX = "payment:saga:order:"
        private val SAGA_TTL = Duration.ofMinutes(10)
    }

    override fun save(saga: PaymentSaga): PaymentSaga {
        val sagaKey = SAGA_KEY_PREFIX + saga.sagaId
        val orderSagaKey = ORDER_SAGA_KEY_PREFIX + saga.orderId

        try {
            val sagaJson = objectMapper.writeValueAsString(saga)

            redisTemplate.opsForValue().set(sagaKey, sagaJson, SAGA_TTL)

            redisTemplate.opsForValue().set(orderSagaKey, saga.sagaId, SAGA_TTL)

            return saga
        } catch (e: Exception) {
            throw RuntimeException("레디스 저장 실패", e)
        }
    }

    override fun findBySagaId(sagaId: String): PaymentSaga? {
        val sagaKey = SAGA_KEY_PREFIX + sagaId

        return try {
            val sagaJson = redisTemplate.opsForValue().get(sagaKey) as? String
            sagaJson?.let {
                objectMapper.readValue(it, PaymentSaga::class.java)
            }
        } catch (e: Exception) {
            throw RuntimeException("sagaId로 PaymentSaga 조회 실패: $sagaId", e)
        }
    }

    override fun findByOrderId(orderId: Long): PaymentSaga? {
        val orderSagaKey = ORDER_SAGA_KEY_PREFIX + orderId

        return try {
            val sagaId = redisTemplate.opsForValue().get(orderSagaKey) as? String
            sagaId?.let { findBySagaId(it) }
        } catch (e: Exception) {
            throw RuntimeException("orderId로 PaymentSaga 조회 실패: $orderId", e)
        }
    }
}