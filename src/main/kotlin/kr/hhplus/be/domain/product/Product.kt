package kr.hhplus.be.domain.product

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.infrastructure.entity.ProductEntity
import java.time.LocalDateTime

data class Product(
    val id: Long,
    val name: String,
    var stock: Int,
    val price: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    fun validateStock(quantity: Int) {
        if (stock < quantity) {
            throw BusinessException(ErrorCode.INSUFFICIENT_STOCK)
        }
    }

    fun deductStock(quantity: Int) {
        validateStock(quantity)
        this.stock -= quantity
    }

    fun toEntity(): ProductEntity {
        return ProductEntity(
            productId = this.id,
            name = this.name,
            price = this.price,
            stock = this.stock,
            status = ProductStatus.ACTIVE,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}

data class ProductRanking(
    val id: Long,
    val name: String,
    val price: Int,
    val totalSalesQuantity: Int,
    val rank: Int
)
