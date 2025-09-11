package kr.hhplus.be.infrastructure.persistence.repository.redis

import kr.hhplus.be.domain.product.ProductRankingCache
import kr.hhplus.be.domain.product.ProductRedissonRepository
import org.redisson.api.RScoredSortedSet
import org.redisson.api.RedissonClient
import org.redisson.client.codec.LongCodec
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.*

@Repository
class ProductRedissonRepositoryImpl(
    private val redissonClient: RedissonClient,
) : ProductRedissonRepository {
    companion object {
        const val PRODUCT_RANKING_DAILY = "product:ranking:daily:%s"
        const val PRODUCT_RANKING_WEEKLY = "product:ranking:weekly:%s"

        val DAILY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val WEEKLY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-'W'ww")
    }

    override fun increaseScore(productId: Long, quantity: Int) {
        val today = LocalDate.now()
        val score = quantity.toDouble()

        val dailyKey = PRODUCT_RANKING_DAILY.format(today.format(DAILY_FORMAT))
        updateScoreForKey(dailyKey, productId, score)

        val weekFields = WeekFields.of(Locale.getDefault())
        val weeklyDate = today.with(weekFields.dayOfWeek(), 1)
        val weeklyKey = PRODUCT_RANKING_WEEKLY.format(weeklyDate.format(WEEKLY_FORMAT))
        updateScoreForKey(weeklyKey, productId, score)
    }

    override fun getDailyTopProducts(date: LocalDate?): List<ProductRankingCache> {
        val targetDate = date ?: LocalDate.now()
        val key = PRODUCT_RANKING_DAILY.format(targetDate.format(DAILY_FORMAT))

        return getTopRankingsFromKey(key)
    }

    override fun getWeeklyTopProducts(date: LocalDate?): List<ProductRankingCache> {
        val targetDate = date ?: LocalDate.now()
        val weekFields = WeekFields.of(Locale.getDefault())
        val weeklyDate = targetDate.with(weekFields.dayOfWeek(), 1)
        val key = PRODUCT_RANKING_WEEKLY.format(weeklyDate.format(WEEKLY_FORMAT))
        return getTopRankingsFromKey(key)
    }

    override fun cleanupDailyRanking(date: LocalDate) {
        val key = PRODUCT_RANKING_DAILY.format(date.format(DAILY_FORMAT))

        val sortedSet = redissonClient.getScoredSortedSet<String>(key, LongCodec.INSTANCE)
        sortedSet.delete()
    }

    override fun cleanupWeeklyRanking(date: LocalDate) {
        val weekFields = WeekFields.of(Locale.getDefault())
        val weeklyDate = date.with(weekFields.dayOfWeek(), 1)
        val weeklyKey = PRODUCT_RANKING_WEEKLY.format(weeklyDate.format(WEEKLY_FORMAT))

        val sortedSet = redissonClient.getScoredSortedSet<String>(weeklyKey, LongCodec.INSTANCE)
        sortedSet.delete()
    }

    private fun updateScoreForKey(key: String, member: Long, score: Double) {
        val sortedSet = redissonClient.getScoredSortedSet<Long>(key, LongCodec.INSTANCE)

        sortedSet.addScore(member, score)
    }

    private fun getTopRankingsFromKey(key: String): List<ProductRankingCache> {
        val scoredSortedSet: RScoredSortedSet<Long> = redissonClient.getScoredSortedSet(key, LongCodec.INSTANCE)

        return scoredSortedSet.entryRangeReversed(0, -1)
            .map { ProductRankingCache(it.value, it.score.toInt()) }
    }
}