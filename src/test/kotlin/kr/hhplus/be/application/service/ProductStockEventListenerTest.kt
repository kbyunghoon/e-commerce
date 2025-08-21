package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.*
import kr.hhplus.be.domain.product.ProductStockHistoryRepository
import kr.hhplus.be.domain.product.StockChangeType
import kr.hhplus.be.domain.product.events.StockChangedEvent

class ProductStockEventListenerTest : BehaviorSpec({

    val productStockHistoryRepository = mockk<ProductStockHistoryRepository>()
    val productService = mockk<ProductService>()
    val productRankingService = mockk<ProductRankingService>()
    val eventListener = ProductStockHistoryEventListener(
        productStockHistoryRepository,
        productService,
        productRankingService,
    )

    Given("재고 변경 이벤트 처리") {
        When("DEDUCT 이벤트를 받을 때") {
            val event = StockChangedEvent(
                productId = 1L,
                changeType = StockChangeType.DEDUCT,
                changeQuantity = 5,
                previousStock = 100,
                currentStock = 95,
                reason = "주문으로 인한 재고 차감"
            )

            every { productService.saveProductStockHistory(any()) } returns mockk()
            every { productRankingService.increaseProductStockCache(any(), any()) } just Runs

            eventListener.handleStockChanged(event)

            Then("히스토리가 저장되고 랭킹이 업데이트되어야 한다") {
                verify(exactly = 1) {
                    productService.saveProductStockHistory(any())
                }
                verify(exactly = 1) {
                    productRankingService.increaseProductStockCache(1L, 5)
                }
            }
        }
    }
})