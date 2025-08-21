package kr.hhplus.be.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.support.concurrent.ConcurrentTestExecutor
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.TestConstructor

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CouponConcurrencyTest(
    private val couponService: CouponService,
    private val redisTemplate: RedisTemplate<String, Any>
) : DescribeSpec({

    val executor = ConcurrentTestExecutor()

    describe("쿠폰 발급 동시성 테스트") {

        it("100명 동시 요청 시 10개만 발급") {
            // given
            val couponId = 1L
            val stock = 10
            val requestCount = 100
            val stockKey = COUPON_STOCK_KEY.format(couponId)
            val issuedKey = COUPON_ISSUED_SET.format(couponId)

            redisTemplate.opsForValue().set(stockKey, stock.toString())
            redisTemplate.delete(issuedKey)

            // when
            val result = executor.executeWithIndex(50, requestCount) { idx ->
                couponService.issue(CouponIssueCommand(idx.toLong() + 1, couponId))
            }

            // then
            result.getSuccessCount().get() shouldBe stock
            result.getFailureCount().get() shouldBe requestCount - stock
        }

        it("같은 사용자 중복 요청 방지") {
            // given
            val userId = 99L
            val couponId = 2L
            val requestCount = 20
            val stockKey = COUPON_STOCK_KEY.format(couponId)
            val issuedKey = COUPON_ISSUED_SET.format(couponId)

            redisTemplate.opsForValue().set(stockKey, "100")
            redisTemplate.delete(issuedKey)

            // when
            val result = executor.execute(10, requestCount) {
                couponService.issue(CouponIssueCommand(userId, couponId))
            }

            // then
            result.getSuccessCount().get() shouldBe 1
            result.getFailureCount().get() shouldBe requestCount - 1
        }

        it("단일 쿠폰 경합 테스트") {
            // given
            val couponId = 3L
            val stock = 1
            val requestCount = 100
            val stockKey = COUPON_STOCK_KEY.format(couponId)
            val issuedKey = COUPON_ISSUED_SET.format(couponId)

            redisTemplate.opsForValue().set(stockKey, stock.toString())
            redisTemplate.delete(issuedKey)

            // when
            val result = executor.executeWithIndex(50, requestCount) { idx ->
                couponService.issue(CouponIssueCommand(idx.toLong() + 1, couponId))
            }

            // then
            result.getSuccessCount().get() shouldBe 1
            result.getFailureCount().get() shouldBe requestCount - 1
        }
    }
}) {
    companion object {
        const val COUPON_STOCK_KEY = "coupon:stock:%s"
        const val COUPON_ISSUED_SET = "coupon:issued:set:%d"
    }
}