package kr.hhplus.be.application.service.saga

interface SagaUserService {
    
    fun deductBalance(userId: Long, amount: Int)
    
    fun refundBalance(userId: Long, amount: Int)
}