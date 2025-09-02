package kr.hhplus.be.global.lock

import java.time.LocalDate

interface LockKeyProvider {
    fun getLockKey(): String
}

data class CouponLockKeyProvider(
    private val couponId: Long
) : LockKeyProvider {
    override fun getLockKey(): String = couponId.toString()
}

data class UserBalanceLockKeyProvider(
    private val userId: Long
) : LockKeyProvider {
    override fun getLockKey(): String = userId.toString()
}

data class ProductStockLockKeyProvider(
    private val productId: Long
) : LockKeyProvider {
    override fun getLockKey(): String = productId.toString()
}

data class ProductRankingLockKeyProvider(
    private val productId: Long,
    private val rankingDate: LocalDate
) : LockKeyProvider {
    override fun getLockKey(): String = "$productId:$rankingDate"
}

data class OrderLockKeyProvider(
    private val userId: Long,
    private val orderId: Long? = null
) : LockKeyProvider {
    override fun getLockKey(): String = orderId?.let { "${userId}:${orderId}" } ?: userId.toString()
}