package kr.hhplus.be.global.lock

import java.util.concurrent.TimeUnit

interface LockStrategyExecutor {
    val lockStrategy: LockStrategy

    @Throws(Throwable::class)
    fun <T> executeWithLock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        callback: LockCallback<T>
    ): T
}