package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    var balance: Int,
    var name: String,
    var email: String,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun chargeBalance(amount: Int) {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)
        }
        this.balance += amount
        this.updatedAt = LocalDateTime.now()
    }

    fun deductBalance(amount: Int) {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.DEDUCT_INVALID_AMOUNT)
        }
        if (this.balance < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_BALANCE)
        }
        this.balance -= amount
        this.updatedAt = LocalDateTime.now()
    }
}