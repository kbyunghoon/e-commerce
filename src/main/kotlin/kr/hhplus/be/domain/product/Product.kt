package kr.hhplus.be.domain.product

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

data class Product(
    val id: Long? = null,
    val name: String,
    val stock: Int,
    val price: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val status: ProductStatus,
    val version: Long = 0L
) {
    fun validateStock(quantity: Int) {
        if (stock < quantity) {
            throw BusinessException(ErrorCode.INSUFFICIENT_STOCK)
        }
    }

    fun deductStock(quantity: Int): Product {
        validateStock(quantity)
        return this.copy(stock = this.stock - quantity, updatedAt = LocalDateTime.now())
    }

    fun addStock(quantity: Int): Product {
        return this.copy(stock = this.stock + quantity, updatedAt = LocalDateTime.now())
    }
}