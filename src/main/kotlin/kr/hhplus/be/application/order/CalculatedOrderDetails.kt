package kr.hhplus.be.application.order

import kr.hhplus.be.application.product.ProductDto

data class CalculatedOrderDetails(
    val totalAmount: Int,
    val discountAmount: Int,
    val finalAmount: Int,
    val products: List<ProductDto.ProductInfo>
)
