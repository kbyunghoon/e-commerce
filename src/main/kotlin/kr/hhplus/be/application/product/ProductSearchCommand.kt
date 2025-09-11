package kr.hhplus.be.application.product

import org.springframework.data.domain.Pageable

data class ProductSearchCommand(
    val pageable: Pageable,
    val search: String?,
    val minPrice: Int?,
    val maxPrice: Int?
)
