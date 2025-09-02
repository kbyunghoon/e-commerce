package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductStockHistoryCommand
import kr.hhplus.be.domain.product.ProductStockHistory
import kr.hhplus.be.domain.product.ProductStockHistoryRepository
import org.springframework.stereotype.Service

@Service
class ProductStockHistoryService(
    private val productStockHistoryRepository: ProductStockHistoryRepository
) {
    fun save(command: ProductStockHistoryCommand): ProductStockHistory {
        val history = ProductStockHistory.create(
            productId = command.productId,
            changeType = command.changeType,
            changeQuantity = command.changeQuantity,
            previousStock = command.previousStock,
            transactionAt = command.transactionAt,
        )

        return productStockHistoryRepository.save(history)
    }
}