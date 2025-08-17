package kr.hhplus.be.infrastructure.lock

import kr.hhplus.be.global.lock.LockCallback
import kr.hhplus.be.global.lock.LockStrategy
import kr.hhplus.be.global.lock.LockStrategyExecutor
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

@Component
class SpinLockExecutor(
    private val redissonClient: RedissonClient
) : LockStrategyExecutor {

    private val log = LoggerFactory.getLogger(javaClass)

    override val lockStrategy: LockStrategy = LockStrategy.SPIN_LOCK

    override fun <T> executeWithLock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        callback: LockCallback<T>
    ): T {
        val lock = redissonClient.getLock(key)
        val timeoutMillis = timeUnit.toMillis(waitTime)
        val spinIntervalMillis = 10L

        log.debug("[SpinLock] 락 획득 시도 시작 - key={}, timeout={}ms", key, timeoutMillis)

        val elapsedTime = measureTimeMillis {
            var acquired = false
            val startTime = System.currentTimeMillis()
            var attemptCount = 0

            while (!acquired && (System.currentTimeMillis() - startTime) < timeoutMillis) {
                attemptCount++

                try {
                    acquired = lock.tryLock(0, leaseTime, timeUnit)

                    if (!acquired) {
                        log.trace("[SpinLock] 락 획득 실패 - 재시도 #{}, key={}", attemptCount, key)

                        Thread.sleep(spinIntervalMillis)
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                    throw RuntimeException("[SpinLock] 락 획득 중 인터럽트 발생 : $key", e)
                }
            }

            if (!acquired) {
                log.warn(
                    "[SpinLock] 락 획득 실패 - key={}, 총 시도 횟수={}, 소요시간={}ms",
                    key, attemptCount, System.currentTimeMillis() - startTime
                )
                throw RuntimeException("[SpinLock] 타임아웃으로 인한 획득 실패 : $key")
            }

            log.debug("[SpinLock] 락 획득 성공 - key={}, 시도 횟수={}", key, attemptCount)
        }

        return try {
            log.debug("[SpinLock] 비즈니스 로직 실행 - key={}, 락 획득 소요시간={}ms", key, elapsedTime)
            callback.doInLock()
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock()
                    log.debug("[SpinLock] 락 해제 완료 - key={}", key)
                } else {
                    log.warn("[SpinLock] 현재 스레드가 소유하지 않은 락 - key={}", key)
                }
            } catch (e: Exception) {
                log.error("[SpinLock] 락 해제 중 오류 발생 - key={}", key, e)
            }
        }
    }
}