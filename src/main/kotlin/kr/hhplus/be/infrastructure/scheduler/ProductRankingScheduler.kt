package kr.hhplus.be.infrastructure.scheduler

import kr.hhplus.be.application.service.ProductRankingService
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component

@Component
@EnableScheduling
class ProductRankingScheduler(private val productRankingService: ProductRankingService) {
}