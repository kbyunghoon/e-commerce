package kr.hhplus.be.domain.model

import java.time.LocalDateTime

data class Product(
    val id: Long,
    val name: String,
    val price: Int,
    val stock: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ProductRanking(
    val id: Long,
    val name: String,
    val price: Int,
    val totalSalesQuantity: Int,
    val rank: Int
)
