package kr.hhplus.be.infrastructure.lock

import kr.hhplus.be.global.lock.LockCallback
import kr.hhplus.be.global.lock.LockStrategy
import kr.hhplus.be.global.lock.LockStrategyExecutor
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class PubSubLockExecutor(
    private val redissonClient: RedissonClient
) : LockStrategyExecutor {

    private val log = LoggerFactory.getLogger(javaClass)

    override val lockStrategy: LockStrategy = LockStrategy.PUB_SUB_LOCK

    override fun <T> executeWithLock(
        key: String,
        waitTime: Long,
        leaseTime: Long,
        timeUnit: TimeUnit,
        callback: LockCallback<T>
    ): T {
        val lock = redissonClient.getFairLock(key)
        log.debug("[PubSubLock] 락 획득 시도 - key={}, waitTime={}, leaseTime={}", key, waitTime, leaseTime)

        val acquired = try {
            lock.tryLock(waitTime, leaseTime, timeUnit)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw RuntimeException("[PubSubLock] 락 획득 중 인터럽트 발생: $key", e)
        }

        if (!acquired) {
            log.warn("[PubSubLock] 락 획득 실패 - key={}, waitTime={}", key, waitTime)
            throw RuntimeException("[PubSubLock] 락 획득 실패 : $key")
        }

        return try {
            log.debug("[PubSubLock] 락 획득 성공, 비즈니스 로직 실행 - key={}", key)
            callback.doInLock()
        } finally {
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock()
                    log.debug("[PubSubLock] 락 해제 완료 - key={}", key)
                } else {
                    log.warn("[PubSubLock] 현재 스레드가 소유하지 않은 락 - key={}", key)
                }
            } catch (e: Exception) {
                log.error("[PubSubLock] 락 해제 중 오류 발생 - key={}", key, e)
            }
        }
    }
}