package kr.hhplus.be.application.dto

import java.time.LocalDateTime

import kr.hhplus.be.adapter.`in`.web.dto.request.BalanceChargeRequest

data class BalanceChargeCommand(
    val userId: Long,
    val amount: Int,
) {
    companion object {
        fun of(request: BalanceChargeRequest): BalanceChargeCommand {
            return BalanceChargeCommand(
                userId = request.userId,
                amount = request.amount
            )
        }
    }
}

data class BalanceInfo(
    val id: Long,
    val userId: Long,
    val amount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
