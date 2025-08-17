package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kr.hhplus.be.application.product.ProductDto.ProductRankingInfo
import kr.hhplus.be.domain.product.ProductRankingRepository

class ProductRankingServiceTest : BehaviorSpec({
    val productRankingRepository: ProductRankingRepository = mockk()
    val productRankingService = ProductRankingService(productRankingRepository)

    afterContainer {
        clearAllMocks()
    }

    Given("인기 상품 조회(getTopProducts) 시나리오") {
        When("인기 상품 목록 조회를 요청하면") {
            val productRankingInfo1 = ProductRankingInfo(1L, "Product A", 1)
            val productRankingInfo2 = ProductRankingInfo(2L, "Product B", 2)
            val mockRankings = listOf(productRankingInfo1, productRankingInfo2)

            every { productRankingRepository.findTopProducts() } returns mockRankings

            val result = productRankingService.getTopProducts()

            Then("인기 상품 목록이 반환된다") {
                result shouldBe mockRankings
            }
        }

        When("인기 상품이 없는 경우 조회를 요청하면") {
            every { productRankingRepository.findTopProducts() } returns emptyList()

            val result = productRankingService.getTopProducts()

            Then("빈 목록이 반환된다") {
                result shouldBe emptyList()
            }
        }
    }
})
