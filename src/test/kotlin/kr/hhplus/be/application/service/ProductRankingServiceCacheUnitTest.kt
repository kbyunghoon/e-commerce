package kr.hhplus.be.application.service

import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.domain.product.RankingPeriod
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDate

@IntegrationTest
class ProductRankingServiceCacheUnitTest(
    @Autowired private val productRankingService: ProductRankingService,
    @MockkBean private val productRankingRepository: ProductRankingRepository
) : FunSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        test("캐시 동작 테스트 - 동일 요청 시 Repository는 한 번만 호출") {
            val rankingDate = LocalDate.now()
            val request = ProductRankingCommand(rankingDate = rankingDate, period = RankingPeriod.THREE_DAYS)

            val productRankingsList = listOf(
                ProductRanking(1L, "상품 1", 100, 1, rankingDate),
                ProductRanking(2L, "상품 2", 90, 2, rankingDate)
            )

            every { productRankingRepository.findTopProducts(any(), any()) } returns productRankingsList

            val result1 = productRankingService.getTopProducts(request)
            val result2 = productRankingService.getTopProducts(request)
            val result3 = productRankingService.getTopProducts(request)

            verify(exactly = 1) { productRankingRepository.findTopProducts(any(), any()) }

            result1 shouldBe result2
            result2 shouldBe result3
        }
    }
}