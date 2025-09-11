package kr.hhplus.be.global.lock

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class LockKeyGenerator(
    private val applicationContext: ApplicationContext
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val PREFIX = "lock:"
    }

    fun generateKey(
        paramNames: Array<String>,
        args: Array<Any?>,
        keyProviderBeanName: String,
        resource: LockResource
    ): String {
        log.debug("[Lock] 키 생성 시작 - keyProvider='{}', resource={}", keyProviderBeanName, resource)

        return try {
            val keyProvider = findKeyProviderFromArgs(args) 
                ?: throw IllegalArgumentException("LockKeyProvider를 찾을 수 없습니다. 메서드 파라미터 중 LockKeyProvider를 구현한 객체가 필요합니다.")

            val key = keyProvider.getLockKey()
            check(key.isNotBlank()) { "LockKeyProvider가 빈 키를 반환했습니다" }

            PREFIX + resource.createKey(key)
        } catch (e: Exception) {
            log.error("락 키 생성 실패 - keyProvider='{}', resource={}", keyProviderBeanName, resource, e)
            throw IllegalArgumentException("락 키 생성에 실패했습니다: ${e.message}", e)
        }
    }

    private fun findKeyProviderFromArgs(args: Array<Any?>): LockKeyProvider? {
        return args.filterNotNull()
            .firstOrNull { it is LockKeyProvider } as? LockKeyProvider
    }
}