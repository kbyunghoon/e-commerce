package kr.hhplus.be.application.service

import kr.hhplus.be.domain.product.ProductStockHistory
import kr.hhplus.be.domain.product.ProductStockHistoryRepository
import kr.hhplus.be.domain.product.events.StockChangedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class ProductStockHistoryEventListener(
    private val productStockHistoryRepository: ProductStockHistoryRepository
) {

    @EventListener
    @Transactional
    fun handleStockChanged(event: StockChangedEvent) {
        val history = ProductStockHistory(
            productId = event.productId,
            changeType = event.changeType,
            changeQuantity = event.changeQuantity,
            previousStock = event.previousStock,
            currentStock = event.currentStock,
            reason = event.reason
        )
        
        productStockHistoryRepository.save(history)
    }
}
