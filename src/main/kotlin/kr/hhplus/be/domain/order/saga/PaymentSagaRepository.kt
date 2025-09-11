package kr.hhplus.be.domain.order.saga

interface PaymentSagaRepository {
    fun save(saga: PaymentSaga): PaymentSaga
    fun findBySagaId(sagaId: String): PaymentSaga?
    fun findByOrderId(orderId: Long): PaymentSaga?
}