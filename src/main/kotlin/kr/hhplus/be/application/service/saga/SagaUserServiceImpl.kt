package kr.hhplus.be.application.service.saga

import kr.hhplus.be.application.service.BalanceService
import org.springframework.stereotype.Service

@Service
class SagaUserServiceImpl(
    private val balanceService: BalanceService
) : SagaUserService {

    override fun deductBalance(userId: Long, amount: Int) {
        balanceService.deductForSaga(userId, amount)
    }

    override fun refundBalance(userId: Long, amount: Int) {
        balanceService.refundForSaga(userId, amount)
    }
}