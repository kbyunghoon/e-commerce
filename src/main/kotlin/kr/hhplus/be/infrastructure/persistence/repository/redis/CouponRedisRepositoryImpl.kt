package kr.hhplus.be.infrastructure.persistence.repository.redis

import kr.hhplus.be.domain.coupon.CouponRedisRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Repository

@Repository
class CouponRedisRepositoryImpl(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val issueRequest: RedisScript<String> = issueRequestScript()
) : CouponRedisRepository {
    companion object {
        const val COUPON_STOCK_KEY = "coupon:stock:%s"
        const val COUPON_ISSUED_SET = "coupon:issued:set:%d"

        private fun issueRequestScript(): RedisScript<String> {
            val luaScript = """
            local stockKey = KEYS[1]
            local issuedKey = KEYS[2] 
            local userId = ARGV[1]
            
            -- 1. 중복 발급 체크 (SADD는 이미 존재하면 0 반환)
            if redis.call('SADD', issuedKey, userId) == 0 then
                return 'ALREADY_ISSUED'
            end
            
            -- 2. 재고 확인 및 차감
            local currentStock = redis.call('GET', stockKey)
            if not currentStock or tonumber(currentStock) <= 0 then
                -- 재고 없으면 Set에서 제거하고 실패
                redis.call('SREM', issuedKey, userId)
                return 'SOLD_OUT'
            end
            
            -- 3. 재고 차감
            local newStock = redis.call('DECR', stockKey)
            if newStock < 0 then
                -- 동시성으로 인한 음수 재고 시 복구
                redis.call('INCR', stockKey)
                redis.call('SREM', issuedKey, userId)
                return 'SOLD_OUT'
            end
            
            return 'SUCCESS'
        """.trimIndent()

            return RedisScript.of(luaScript, String::class.java)
        }
    }

    override fun issueRequest(userId: Long, couponId: Long): String {
        val stockKey = COUPON_STOCK_KEY.format(couponId)
        val issuedKey = COUPON_ISSUED_SET.format(couponId)

        return redisTemplate.execute(
            issueRequest,
            listOf(stockKey, issuedKey),
            userId.toString()
        )
    }
}