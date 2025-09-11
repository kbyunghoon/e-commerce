package kr.hhplus.be.global.lock

import java.util.concurrent.TimeUnit

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class DistributedLock(
    val key: String,

    val timeUnit: TimeUnit = TimeUnit.SECONDS,

    val waitTime: Long = 5L,

    val leaseTime: Long = 3L,

    val lockStrategy: LockStrategy = LockStrategy.PUB_SUB_LOCK,

    val resource: LockResource,
)