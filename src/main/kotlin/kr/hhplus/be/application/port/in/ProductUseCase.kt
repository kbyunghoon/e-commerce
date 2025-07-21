package kr.hhplus.be.application.port.`in`

import kr.hhplus.be.application.dto.ProductInfo

interface ProductUseCase {
    fun getProducts(): List<ProductInfo>
    fun getProduct(productId: Long): ProductInfo
}
