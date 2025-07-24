package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.infrastructure.entity.UserEntity
import java.time.LocalDateTime

data class User(
    val id: Long = 0,
    val name: String,
    val email: String,
    var balance: Int,
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

    fun toEntity(): UserEntity {
        return UserEntity(
            id = this.id,
            name = this.name,
            email = this.email,
            balance = this.balance,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}