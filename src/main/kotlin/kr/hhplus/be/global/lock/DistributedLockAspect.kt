package kr.hhplus.be.global.lock

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Aspect
@Component
class DistributedLockAspect(
    private val generator: LockKeyGenerator,
    private val registry: LockStrategyRegistry
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Around("@annotation(kr.hhplus.be.global.lock.DistributedLock)")
    fun around(joinPoint: ProceedingJoinPoint): Any? {
        val sig = joinPoint.signature as MethodSignature
        val ann = sig.method.getAnnotation(DistributedLock::class.java)

        log.debug("[Lock] AOP 시작 - method={}", sig.toShortString())

        val key = generator.generateKey(
            sig.parameterNames,
            joinPoint.args,
            ann.key,
            ann.resource
        )
        log.debug("[Lock] 키 생성 - $key")

        val lock = registry.getLockStrategyExecutor(ann.lockStrategy)
        log.debug("[Lock] 락 종류 - {}", lock.javaClass.simpleName)

        return lock.executeWithLock(
            key,
            ann.waitTime,
            ann.leaseTime,
            ann.timeUnit
        ) { joinPoint.proceed() }
    }
}