package kr.hhplus.be.application.facade

import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceInfo
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.presentation.dto.response.BalanceChargeResponse
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class BalanceFacade(
    private val balanceService: BalanceService
) {

    @Transactional
    fun chargeBalance(request: BalanceChargeCommand): BalanceChargeResponse {
        val balanceBeforeCharge = balanceService.getBalance(request.userId).amount

        val balanceAfterCharge = balanceService.charge(request).amount

        recordChargeHistory(request.userId, balanceBeforeCharge, request.amount)

        return BalanceChargeResponse(
            userId = request.userId,
            balance = balanceAfterCharge,
            chargedAmount = request.amount,
            chargedAt = java.time.LocalDateTime.now()
        )
    }

    private fun recordChargeHistory(userId: Long, balanceBeforeCharge: Int, chargeAmount: Int) {
        balanceService.recordChargeHistory(userId, balanceBeforeCharge, chargeAmount)
    }

    @Transactional
    fun deductBalance(request: BalanceDeductCommand): BalanceInfo {
        val balanceBeforeDeduct = balanceService.getBalance(request.userId).amount

        balanceService.use(request)

        recordDeductHistory(request.userId, balanceBeforeDeduct, request.amount)

        return balanceService.getBalance(request.userId)
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: Long): BalanceInfo {
        return balanceService.getBalance(userId)
    }

    private fun recordDeductHistory(userId: Long, balanceBeforeDeduct: Int, deductAmount: Int) {
        balanceService.recordDeductHistory(userId, balanceBeforeDeduct, deductAmount)
    }
}
