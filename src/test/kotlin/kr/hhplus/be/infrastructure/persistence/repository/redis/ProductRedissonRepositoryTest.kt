package kr.hhplus.be.infrastructure.persistence.repository.redis

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.config.IntegrationTest
import org.redisson.api.RedissonClient
import java.time.LocalDate

@IntegrationTest
class ProductRedissonRepositoryTest(
    private val productRedissonRepository: ProductRedissonRepositoryImpl,
    private val redissonClient: RedissonClient
) : BehaviorSpec({


    Given("상품 점수 증가 테스트") {
        When("단일 상품의 점수를 증가시킬 때") {
            redissonClient.keys.flushdb()
            val productId = 1L
            val quantity = 5
            val date = LocalDate.now()

            productRedissonRepository.increaseScore(productId, quantity)

            Then("일간 랭킹에 점수가 반영되어야 한다") {
                val dailyRankings = productRedissonRepository.getDailyTopProducts(date)

                dailyRankings.size shouldBe 1
                dailyRankings[0].productId shouldBe productId
                dailyRankings[0].totalSalesCount shouldBe quantity
            }

            Then("주간 랭킹에 점수가 반영되어야 한다") {
                val weeklyRankings = productRedissonRepository.getWeeklyTopProducts(date)

                weeklyRankings.size shouldBe 1
                weeklyRankings[0].productId shouldBe productId
                weeklyRankings[0].totalSalesCount shouldBe quantity
            }
        }

        When("같은 상품의 점수를 여러 번 증가시킬 때") {
            redissonClient.keys.flushdb()
            val productId = 1L
            val date = LocalDate.now()

            productRedissonRepository.increaseScore(productId, 3)
            productRedissonRepository.increaseScore(productId, 7)

            Then("점수가 누적되어야 한다") {
                val rankings = productRedissonRepository.getDailyTopProducts(date)

                rankings[0].productId shouldBe productId
                rankings[0].totalSalesCount shouldBe 10
            }
        }

        When("여러 상품의 점수를 증가시킬 때") {
            redissonClient.keys.flushdb()

            val date = LocalDate.now()
            productRedissonRepository.increaseScore(1L, 10)
            productRedissonRepository.increaseScore(2L, 15)
            productRedissonRepository.increaseScore(3L, 5)

            Then("점수 순으로 정렬되어야 한다") {
                val rankings = productRedissonRepository.getDailyTopProducts(date)

                rankings.size shouldBe 3
                rankings[0].productId shouldBe 2L
                rankings[1].productId shouldBe 1L
                rankings[2].productId shouldBe 3L
            }
        }
    }
})