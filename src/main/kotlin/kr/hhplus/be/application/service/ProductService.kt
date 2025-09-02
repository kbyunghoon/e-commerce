package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.product.CreateProductRanking
import kr.hhplus.be.application.product.ProductSearchCommand
import kr.hhplus.be.application.product.ProductStockHistoryCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.*
import kr.hhplus.be.global.lock.DistributedLock
import kr.hhplus.be.global.lock.LockResource
import kr.hhplus.be.global.lock.LockStrategy
import kr.hhplus.be.global.lock.ProductStockLockKeyProvider
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val productStockHistoryRepository: ProductStockHistoryRepository,
    private val productStockHistoryService: ProductStockHistoryService,
    private val productRankingService: ProductRankingService,
    private val productRankingRepository: ProductRankingRepository
) {

    @Transactional(readOnly = true)
    fun getProducts(
        command: ProductSearchCommand
    ): Page<Product> {
        return productRepository.findAvailableProducts(
            command.pageable,
            command.search,
            command.minPrice,
            command.maxPrice
        )
    }

    @Transactional(readOnly = true)
    fun getAllProducts(pageable: Pageable): Page<Product> {
        return productRepository.findAll(pageable)
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: Long): Product {
        return productRepository.findByIdOrThrow(productId)
    }

    fun getProductsByIds(productIds: List<Long>): List<Product> {
        return productRepository.findByProductIds(productIds)
    }

    fun validateOrderItems(items: List<OrderItemCreateCommand>): List<Product> {
        val productIds = items.map { it.productId }
        val products = productRepository.findByProductIds(productIds)

        if (products.size != productIds.size) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }

        items.forEach { item ->
            val product = products.find { it.id == item.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            validateStock(product, item.quantity)
        }

        return products
    }

    fun validateStock(product: Product, requestQuantity: Int) {
        if (product.stock < requestQuantity) {
            throw BusinessException(ErrorCode.INSUFFICIENT_STOCK)
        }
    }

    @DistributedLock(
        resource = LockResource.PRODUCT_STOCK,
        keyProvider = "productStockLockKeyProvider",
        lockStrategy = LockStrategy.PUB_SUB_LOCK,
        waitTime = 5,
        leaseTime = 20
    )
    @Transactional
    fun deductStock(
        productId: Long,
        quantity: Int,
        keyProvider: ProductStockLockKeyProvider = ProductStockLockKeyProvider(productId)
    ): Product {
        val product = productRepository.findByIdWithPessimisticLock(productId)
        val previousStock = product.stock

        val updatedProduct = productRepository.save(product.deductStock(quantity))

        val stockHistory = ProductStockHistoryCommand(
            productId = product.id!!,
            changeType = StockChangeType.DEDUCT,
            changeQuantity = quantity,
            previousStock = previousStock,
            transactionAt = updatedProduct.updatedAt
        )
        productStockHistoryService.save(stockHistory)

        productRankingService.updateSalesCount(
            CreateProductRanking(
                productId = product.id,
                productName = product.name,
                quantity = quantity,
                rankingDate = updatedProduct.updatedAt.toLocalDate()
            )
        )

        return updatedProduct
    }

    @DistributedLock(
        resource = LockResource.PRODUCT_STOCK,
        keyProvider = "productStockLockKeyProvider",
        lockStrategy = LockStrategy.PUB_SUB_LOCK,
        waitTime = 5,
        leaseTime = 10
    )
    @Transactional
    fun restoreStock(
        productId: Long,
        quantity: Int,
        keyProvider: ProductStockLockKeyProvider = ProductStockLockKeyProvider(productId)
    ): Product {
        val product = productRepository.findByIdOrThrow(productId)
        val previousStock = product.stock

        val updatedProduct = productRepository.save(product.addStock(quantity))

        val command = ProductStockHistoryCommand(
            productId = product.id!!,
            changeType = StockChangeType.RESTORE,
            changeQuantity = quantity,
            previousStock = previousStock,
            transactionAt = updatedProduct.updatedAt
        )
        productStockHistoryService.save(command)

        productRankingService.updateSalesCount(
            CreateProductRanking(
                productId = product.id,
                productName = product.name,
                quantity = -quantity,
                rankingDate = updatedProduct.updatedAt.toLocalDate()
            )
        )

        return updatedProduct
    }
}
