package kr.hhplus.be.application.balance

data class BalanceChargeCommand(
    val userId: Long,
    val amount: Int,
)

data class BalanceDeductCommand(
    val userId: Long,
    val amount: Int
)
