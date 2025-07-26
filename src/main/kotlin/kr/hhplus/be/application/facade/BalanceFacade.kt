package kr.hhplus.be.application.facade

import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceDto.BalanceInfo
import kr.hhplus.be.application.service.BalanceService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class BalanceFacade(
    private val balanceService: BalanceService
) {

    data class BalanceChargeResult(
        val userId: Long,
        val balance: Int,
        val chargedAmount: Int,
        val chargedAt: LocalDateTime
    )

    @Transactional
    fun chargeBalance(request: BalanceChargeCommand): BalanceChargeResult {
        val balanceBeforeCharge = balanceService.getBalance(request.userId).amount

        val balanceAfterCharge = balanceService.charge(request).amount

        recordChargeHistory(request.userId, balanceBeforeCharge, request.amount)

        return BalanceChargeResult(
            userId = request.userId,
            balance = balanceAfterCharge,
            chargedAmount = request.amount,
            chargedAt = LocalDateTime.now()
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