package kr.hhplus.be.application.service

import kr.hhplus.be.application.dto.ProductInfo
import kr.hhplus.be.application.port.`in`.ProductUseCase
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService : ProductUseCase {
    override fun getProducts(): List<ProductInfo> {
        // TODO: 추후 DB에서 상품 목록 조회 로직 추가 예정
        return listOf(
            ProductInfo(
                id = 1L,
                name = "상품명1",
                price = 10000,
                stock = 150,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            ),
            ProductInfo(
                id = 2L,
                name = "상품명2",
                price = 20000,
                stock = 200,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        )
    }

    override fun getProduct(productId: Long): ProductInfo {
        if (productId == 999L) {
            throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
        }
        // TODO: 추후 DB에서 특정 상품 조회 로직 추가 예정
        return ProductInfo(
            id = productId,
            name = "상품명",
            price = 89000,
            stock = 150,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
