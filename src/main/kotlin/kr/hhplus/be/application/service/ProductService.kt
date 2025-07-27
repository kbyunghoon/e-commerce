package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.ProductRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository
) {

    fun getProducts(
        pageable: Pageable,
        searchKeyword: String?,
        minPrice: Int?,
        maxPrice: Int?
    ): Page<ProductDto.ProductInfo> {
        return productRepository.findAvailableProducts(pageable, searchKeyword, minPrice, maxPrice).map { product ->
            ProductDto.ProductInfo.from(product)
        }
    }

    fun getAllProducts(pageable: Pageable): Page<ProductDto.ProductInfo> {
        val products = productRepository.findAll(pageable)
        return products.map { ProductDto.ProductInfo.from(it) }
    }

    fun getProduct(productId: Long): ProductDto.ProductInfo {
        val product = productRepository.findByIdOrThrow(productId)

        return ProductDto.ProductInfo.from(product)
    }

    fun getProductsByIds(productIds: List<Long>): List<ProductDto.ProductInfo> {
        val products = productRepository.findByProductIds(productIds)
        return products.map { ProductDto.ProductInfo.from(it) }
    }

    fun searchProductsByName(pageable: Pageable, keyword: String): Page<ProductDto.ProductInfo> {
        val products = productRepository.findByNameContaining(pageable, keyword)
        return products.map { ProductDto.ProductInfo.from(it) }
    }

    fun getProductsByPriceRange(pageable: Pageable, minPrice: Int, maxPrice: Int): Page<ProductDto.ProductInfo> {
        val products = productRepository.findByPriceBetween(pageable, minPrice, maxPrice)
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

    fun deductStock(productId: Long, quantity: Int) {
        val product = productRepository.findByIdOrThrow(productId)

        product.deductStock(quantity)
        productRepository.save(product)
    }

    fun restoreStock(productId: Long, quantity: Int) {
        val product = productRepository.findByIdOrThrow(productId)

        product.addStock(quantity)
        productRepository.save(product)
    }
}
