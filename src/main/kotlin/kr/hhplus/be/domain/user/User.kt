package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

data class User(
    val id: Long? = null,
    val balance: Int = 0,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val version: Long = 0
) {
    fun chargeBalance(amount: Int): User {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)
        }

        return this.copy(balance = balance + amount, updatedAt = LocalDateTime.now())
    }

    fun deductBalance(amount: Int): User {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.DEDUCTION_INVALID_AMOUNT)
        }
        if (this.balance < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_BALANCE)
        }

        return this.copy(balance = balance - amount, updatedAt = LocalDateTime.now())

    }
}