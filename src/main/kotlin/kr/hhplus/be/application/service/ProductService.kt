package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.domain.product.StockChangeType
import kr.hhplus.be.domain.product.events.StockChangedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {

    @Transactional(readOnly = true)
    fun getProducts(
        pageable: Pageable,
        searchKeyword: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): Page<ProductDto.ProductInfo> {
        return productRepository.findAvailableProducts(pageable, searchKeyword, minPrice, maxPrice)
            .map { ProductDto.ProductInfo.from(it) }
    }

    @Transactional(readOnly = true)
    fun getAllProducts(pageable: Pageable): Page<ProductDto.ProductInfo> {
        val products = productRepository.findAll(pageable)
        return products.map { ProductDto.ProductInfo.from(it) }
    }

    @Transactional(readOnly = true)
    fun getProduct(productId: Long): ProductDto.ProductInfo {
        val product = productRepository.findByIdOrThrow(productId)

        return ProductDto.ProductInfo.from(product)
    }

    fun getProductsByIds(productIds: List<Long>): List<ProductDto.ProductInfo> {
        val products = productRepository.findByProductIds(productIds)
        return products.map { ProductDto.ProductInfo.from(it) }
    }

    fun validateOrderItems(items: List<OrderItemCreateCommand>): List<ProductDto.ProductInfo> {
        val productIds = items.map { it.productId }
        val products = productRepository.findByProductIds(productIds)

        if (products.size != productIds.size) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }

        items.forEach { item ->
            val product = products.find { it.id == item.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            validateStock(ProductDto.ProductInfo.from(product), item.quantity)
        }

        return products.map { ProductDto.ProductInfo.from(it) }
    }

    fun validateStock(product: ProductDto.ProductInfo, requestQuantity: Int) {
        if (product.stock < requestQuantity) {
            throw BusinessException(ErrorCode.INSUFFICIENT_STOCK)
        }
    }

    @Transactional
    fun deductStock(productId: Long, quantity: Int) {
        val product = productRepository.findByIdWithPessimisticLock(productId)
        val previousStock = product.stock

        product.deductStock(quantity)
        val updatedProduct = productRepository.save(product)

        applicationEventPublisher.publishEvent(
            StockChangedEvent(
                productId = productId,
                changeType = StockChangeType.DEDUCT,
                changeQuantity = quantity,
                previousStock = previousStock,
                currentStock = updatedProduct.stock,
                reason = StockChangeType.DEDUCT.reason
            )
        )
    }

    @Transactional
    fun batchDeductStock(stockDeductions: List<ProductDto.ProductStockDeduction>) {
        if (stockDeductions.isEmpty()) return
        
        val sortedDeductions = stockDeductions.sortedBy { it.productId }
        val productIds = sortedDeductions.map { it.productId }
        
        val products = productRepository.findByIdsWithPessimisticLock(productIds)
        
        sortedDeductions.forEach { deduction ->
            val product = products.find { it.id == deduction.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
            
            if (product.stock < deduction.quantity) {
                throw BusinessException(ErrorCode.INSUFFICIENT_STOCK)
            }
        }
        
        val updatedProducts = products.map { product ->
            val deduction = sortedDeductions.find { it.productId == product.id }!!
            val previousStock = product.stock
            
            product.deductStock(deduction.quantity)
            
            applicationEventPublisher.publishEvent(
                StockChangedEvent(
                    productId = product.id,
                    changeType = StockChangeType.DEDUCT,
                    changeQuantity = deduction.quantity,
                    previousStock = previousStock,
                    currentStock = product.stock,
                    reason = StockChangeType.DEDUCT.reason
                )
            )
            
            product
        }
        
        productRepository.saveAll(updatedProducts)
    }

    @Transactional
    fun restoreStock(productId: Long, quantity: Int) {
        val product = productRepository.findByIdOrThrow(productId)
        val previousStock = product.stock

        product.addStock(quantity)
        val updatedProduct = productRepository.save(product)

        applicationEventPublisher.publishEvent(
            StockChangedEvent(
                productId = productId,
                changeType = StockChangeType.RESTORE,
                changeQuantity = quantity,
                previousStock = previousStock,
                currentStock = updatedProduct.stock,
                reason = StockChangeType.RESTORE.reason
            )
        )
    }
}
