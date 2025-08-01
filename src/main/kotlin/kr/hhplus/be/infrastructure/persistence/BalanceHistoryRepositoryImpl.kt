package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.BalanceHistoryRepository
import kr.hhplus.be.infrastructure.persistence.repository.BalanceHistoryJpaRepository
import org.springframework.stereotype.Component

@Component
class BalanceHistoryRepositoryImpl(
    private val balanceHistoryJpaRepository: BalanceHistoryJpaRepository
) : BalanceHistoryRepository {
    override fun save(balanceHistory: BalanceHistory): BalanceHistory {
        TODO("구현 예정")
    }

    override fun findByUserId(userId: Long): List<BalanceHistory> {
        TODO("구현 예정")
    }
}
