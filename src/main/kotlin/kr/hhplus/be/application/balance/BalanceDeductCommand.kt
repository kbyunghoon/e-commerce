package kr.hhplus.be.application.balance

data class BalanceDeductCommand(
    val userId: Long,
    val amount: Int
)