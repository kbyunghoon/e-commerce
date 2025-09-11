package kr.hhplus.be.application.service.saga

interface SagaProductService {

    fun deductStock(orderId: Long, userId: Long)

    fun restoreStock(orderId: Long, userId: Long)
}