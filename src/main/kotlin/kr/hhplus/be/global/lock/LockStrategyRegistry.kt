package kr.hhplus.be.global.lock

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*

@Component
class LockStrategyRegistry(templates: List<LockStrategyExecutor>) {

    private val log = LoggerFactory.getLogger(javaClass)
    private val lockMap: Map<LockStrategy, LockStrategyExecutor> = EnumMap<LockStrategy, LockStrategyExecutor>(LockStrategy::class.java).apply {
        templates.forEach { template ->
            put(template.lockStrategy, template)
        }
    }

    fun getLockStrategyExecutor(strategy: LockStrategy): LockStrategyExecutor {
        log.debug("[Lock] 전략 선택 - strategy={}", strategy)
        return lockMap[strategy] ?: throw IllegalArgumentException("해당 전략을 찾을 수 없음: $strategy")
    }
}