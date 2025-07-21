package kr.hhplus.be.application.service

import kr.hhplus.be.application.dto.ProductRankingInfo
import kr.hhplus.be.application.port.`in`.ProductRankingUseCase
import org.springframework.stereotype.Service

@Service
class ProductRankingService : ProductRankingUseCase {
    override fun getTopProducts(limit: Int): List<ProductRankingInfo> {
        // TODO: 판매량 등을 기준으로 랭킹을 계산하고 조회하는 로직 추가 예정
        return listOf(
            ProductRankingInfo(
                productId = 1L,
                productName = "인기 상품 A",
                rank = 1,
                score = 95.5
            ),
            ProductRankingInfo(
                productId = 2L,
                productName = "인기 상품 B",
                rank = 2,
                score = 92.3
            )
        ).take(limit)
    }
}
