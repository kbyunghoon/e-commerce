package kr.hhplus.be.domain.user

interface BalanceHistoryRepository {
    fun save(balanceHistory: BalanceHistory): BalanceHistory
    fun findByUserId(userId: Long): List<BalanceHistory>
}
