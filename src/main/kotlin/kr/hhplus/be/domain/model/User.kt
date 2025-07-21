package kr.hhplus.be.domain.model

import java.time.LocalDateTime

data class User(
    val userId: Long = 0,
    val name: String,
    val email: String,
    var balance: Int,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun chargeBalance(amount: Int) {
        if (amount <= 0) {
            throw IllegalArgumentException("충전 금액은 0보다 커야 합니다.")
        }
        this.balance += amount
        this.updatedAt = LocalDateTime.now()
    }

    fun deductBalance(amount: Int) {
        if (amount <= 0) {
            throw IllegalArgumentException("차감 금액은 0보다 커야 합니다.")
        }
        if (this.balance < amount) {
            throw IllegalArgumentException("잔액이 부족합니다.")
        }
        this.balance -= amount
        this.updatedAt = LocalDateTime.now()
    }
}