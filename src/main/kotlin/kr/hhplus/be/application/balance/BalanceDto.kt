package kr.hhplus.be.application.balance

import kr.hhplus.be.domain.user.User
import java.time.LocalDateTime

data class BalanceChargeCommand(
    val userId: Long,
    val amount: Int,
) {

}

data class BalanceInfo(
    val id: Long,
    val userId: Long,
    val amount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
) {
    companion object {
        fun from(user: User): BalanceInfo {
            return BalanceInfo(
                id = user.id,
                userId = user.id,
                amount = user.balance,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}

