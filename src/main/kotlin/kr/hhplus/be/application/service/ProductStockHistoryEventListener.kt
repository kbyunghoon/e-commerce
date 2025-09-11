package kr.hhplus.be.application.service

import kr.hhplus.be.domain.product.ProductStockHistory
import kr.hhplus.be.domain.product.ProductStockHistoryRepository
import kr.hhplus.be.domain.product.StockChangeType
import kr.hhplus.be.domain.product.events.StockChangedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ProductStockHistoryEventListener(
    private val productStockHistoryRepository: ProductStockHistoryRepository,
    private val productService: ProductService,
    private val productRankingService: ProductRankingService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleStockChanged(event: StockChangedEvent) {
        val history = ProductStockHistory(
            productId = event.productId,
            changeType = event.changeType,
            changeQuantity = event.changeQuantity,
            previousStock = event.previousStock,
            currentStock = event.currentStock,
            reason = event.reason
        )

        productService.saveProductStockHistory(history)

        when (event.changeType) {
            StockChangeType.DEDUCT -> productRankingService.increaseProductStockCache(
                event.productId,
                event.changeQuantity
            )

            else -> {}
        }
    }
}
