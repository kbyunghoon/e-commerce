package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceHistoryCommand
import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.BalanceHistoryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceHistoryService(
    private val balanceHistoryRepository: BalanceHistoryRepository
) {
    @Transactional
    fun save(command: BalanceHistoryCommand): BalanceHistory {
        val history = BalanceHistory.from(command)

        return balanceHistoryRepository.save(history)
    }
}
