package kr.hhplus.be.application.service

import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.verify
import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.domain.product.RankingPeriod
import kr.hhplus.be.config.IntegrationTest
import org.springframework.beans.factory.annotation.Autowired
import com.ninjasquad.springmockk.SpykBean
import java.time.LocalDate

@IntegrationTest
class ProductRankingServiceCacheTest(
    @Autowired private val productRankingService: ProductRankingService,
    @SpykBean private val productRankingRepository: ProductRankingRepository
) : FunSpec() {

    override fun extensions() = listOf(SpringExtension)

    init {
        beforeEach {
            clearMocks(productRankingRepository)
        }

        test("인기 상품 조회 시 동일 요청은 캐시를 사용하여 리포지토리를 한 번만 호출한다") {
            // Given
            val rankingDate = LocalDate.now()
            val request = ProductRankingCommand(rankingDate = rankingDate, period = RankingPeriod.THREE_DAYS)

            // When
            productRankingService.getTopProductsV1(request)
            productRankingService.getTopProductsV1(request)
            productRankingService.getTopProductsV1(request)

            // Then
            verify(exactly = 1) {
                productRankingRepository.findTopProducts(any(), any())
            }
        }
    }
}
