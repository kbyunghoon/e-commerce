package kr.hhplus.be.application.dto

import java.time.LocalDateTime

data class ProductInfo(
    val id: Long,
    val name: String,
    val price: Int,
    val stock: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)
