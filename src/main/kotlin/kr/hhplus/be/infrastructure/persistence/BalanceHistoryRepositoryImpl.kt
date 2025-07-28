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
        return balanceHistoryJpaRepository.save(balanceHistory.toEntity()).toDomain()
    }

    override fun findByUserId(userId: Long): List<BalanceHistory> {
        return balanceHistoryJpaRepository.findByUserId(userId).map { it.toDomain() }
    }
}
