package kr.hhplus.be.global.lock

import org.slf4j.LoggerFactory
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.stereotype.Component

@Component
class LockKeyGenerator {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        private const val PREFIX = "lock:"
    }

    fun generateKey(
        paramNames: Array<String>,
        args: Array<Any?>,
        spelKey: String,
        resource: LockResource
    ): String {
        log.debug("[Lock] 키 생성 시작 - spel='{}', resource={}", spelKey, resource)

        return try {
            val parser = SpelExpressionParser()
            val context = StandardEvaluationContext()

            paramNames.forEachIndexed { index, name ->
                context.setVariable(name, args[index])
            }

            val resolved = parser.parseExpression(spelKey).getValue(context, String::class.java)
            check(!resolved.isNullOrBlank()) { "SpEL 표현식이 빈 값을 반환했습니다" }

            PREFIX + resource.createKey(resolved)
        } catch (e: Exception) {
            log.error("락 키 생성 실패 - spel='{}', resource={}", spelKey, resource, e)
            throw IllegalArgumentException("락 키 생성에 실패했습니다: ${e.message}", e)
        }
    }
}