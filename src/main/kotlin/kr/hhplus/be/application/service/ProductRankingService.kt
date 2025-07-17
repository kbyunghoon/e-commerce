package kr.hhplus.be.application.service

import kr.hhplus.be.domain.model.ProductRanking
import org.springframework.stereotype.Service

@Service
class ProductRankingService {
    fun getTopProducts(): List<ProductRanking> {
        val product = ProductRanking(
            id = 1,
            name = "상품명",
            price = 10000,
            totalSalesQuantity = 150,
            rank = 1
        )
        return listOf(product)
    }
}
