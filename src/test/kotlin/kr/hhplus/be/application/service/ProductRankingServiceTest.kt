package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.application.product.ProductRankingDtoV1
import kr.hhplus.be.application.product.ProductRankingDtoV2
import kr.hhplus.be.domain.product.ProductRanking
import kr.hhplus.be.domain.product.ProductRankingRepository
import kr.hhplus.be.domain.product.ProductRedissonRepository
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.domain.product.RankingPeriod
import java.time.LocalDate

class ProductRankingServiceTest : BehaviorSpec({
    val productRankingRepository: ProductRankingRepository = mockk()
    val productRedissonRepository: ProductRedissonRepository = mockk()
    val productRepository: ProductRepository = mockk()
    val productRankingService = ProductRankingService(
        productRankingRepository,
        productRedissonRepository,
        productRepository
    )

    afterContainer {
        clearAllMocks()
    }

    Given("인기 상품 조회(getTopProductsV1()) 시나리오") {
        When("인기 상품 목록 조회를 요청하면") {
            val command = ProductRankingCommand(
                rankingDate = LocalDate.now(),
                period = RankingPeriod.DAILY
            )
            val productRankingInfo1 = ProductRanking(
                productId = 1L,
                productName = "상품 A",
                totalSalesCount = 10,
                rankingDate = LocalDate.now(),
                rank = 1
            )
            val productRankingInfo2 = ProductRanking(
                productId = 1L,
                productName = "상품 B",
                totalSalesCount = 5,
                rankingDate = LocalDate.now(),
                rank = 2
            )
            val mockRankings = listOf(productRankingInfo1, productRankingInfo2)
            val mockRankingInfo = mockRankings.map { ProductRankingDtoV1.ProductRankingInfo.from(it) }

            every { productRankingRepository.findTopProducts(LocalDate.now(), LocalDate.now()) } returns mockRankings

            val result = productRankingService.getTopProductsV1(command)

            Then("인기 상품 목록이 반환된다") {
                result shouldBe mockRankingInfo
            }
        }

        When("인기 상품이 없는 경우 조회를 요청하면") {
            val command = ProductRankingCommand(
                rankingDate = LocalDate.now(),
                period = RankingPeriod.DAILY
            )

            every { productRankingRepository.findTopProducts(LocalDate.now(), LocalDate.now()) } returns emptyList()

            val result = productRankingService.getTopProductsV1(command)

            Then("빈 목록이 반환된다") {
                result shouldBe emptyList()
            }
        }
    }
})
