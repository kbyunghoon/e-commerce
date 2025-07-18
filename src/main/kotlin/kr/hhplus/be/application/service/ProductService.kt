package kr.hhplus.be.application.service

import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.model.Product
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService {
    fun getProducts(page: Int, size: Int, search: String?, minPrice: Int?, maxPrice: Int?): List<Product> {
        val product = Product(
            id = 1,
            name = "상품명",
            price = 10000,
            stock = 150,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        return listOf(product)
    }

    fun getProduct(productId: Long): Product? {
        if (productId == 999L) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }
        return Product(
            id = productId,
            name = "상품명",
            price = 89000,
            stock = 150,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
