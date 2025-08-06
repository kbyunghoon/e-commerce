package kr.hhplus.be.application.product

import kr.hhplus.be.domain.product.Product
import java.time.LocalDateTime

class ProductDto {

    data class ProductInfo(
        val id: Long,
        val name: String,
        val price: Int,
        val stock: Int,
        val createdAt: LocalDateTime,
        val updatedAt: LocalDateTime,
    ) {
        companion object {
            fun from(product: Product): ProductInfo {
                return ProductInfo(
                    id = product.id,
                    name = product.name,
                    price = product.price,
                    stock = product.stock,
                    createdAt = product.createdAt,
                    updatedAt = product.updatedAt,
                )
            }
        }
    }

    data class ProductWithStock(
        val product: ProductInfo,
        val availableStock: Int,
        val reservedStock: Int = 0
    )

    data class ProductSearchCriteria(
        val keyword: String? = null,
        val minPrice: Int? = null,
        val maxPrice: Int? = null,
        val page: Int = 0,
        val size: Int = 10
    )

    data class ProductRankingInfo(
        val productId: Long,
        val productName: String,
        val rank: Int,
    )

    data class ProductStockDeduction(
        val productId: Long,
        val quantity: Int
    )
}