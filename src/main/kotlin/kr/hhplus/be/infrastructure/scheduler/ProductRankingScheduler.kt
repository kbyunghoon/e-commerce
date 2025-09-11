package kr.hhplus.be.infrastructure.scheduler

import kr.hhplus.be.application.service.ProductRankingService
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
@EnableScheduling
class ProductRankingScheduler(private val productRankingService: ProductRankingService) {
    @Scheduled(cron = "0 0 2 * * *")
    fun initializeDailyRanking() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        productRankingService.backupDailyRanking(yesterday)

        productRankingService.cleanupDailyRanking(yesterday)
    }

    @Scheduled(cron = "0 0 3 * * MON")
    fun initializeWeeklyRanking() {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        productRankingService.cleanupWeeklyRanking(yesterday)
    }
}