package kr.hhplus.be.application.port.`in`

import kr.hhplus.be.application.dto.BalanceChargeCommand
import kr.hhplus.be.application.dto.BalanceInfo

interface BalanceUseCase {
    fun charge(command: BalanceChargeCommand): BalanceInfo
    fun getBalance(userId: Long): BalanceInfo
}
