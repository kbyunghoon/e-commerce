package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.order.saga.PaymentSaga
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class PaymentSagaRepositoryImpl : PaymentSagaRepository {
    
    private val sagaStore = ConcurrentHashMap<String, PaymentSaga>()

    override fun save(saga: PaymentSaga): PaymentSaga {
        sagaStore[saga.sagaId] = saga
        return saga
    }

    override fun findBySagaId(sagaId: String): PaymentSaga? {
        return sagaStore[sagaId]
    }

    override fun findByOrderId(orderId: Long): PaymentSaga? {
        return sagaStore.values.find { it.orderId == orderId }
    }
}