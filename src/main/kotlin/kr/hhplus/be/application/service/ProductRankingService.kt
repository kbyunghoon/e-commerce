package kr.hhplus.be.application.service

import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.application.product.ProductRankingDto
import kr.hhplus.be.domain.product.ProductRankingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ProductRankingService(
    private val productRankingRepository: ProductRankingRepository
) {
    @Transactional(readOnly = true)
    fun getTopProducts(request: ProductRankingCommand): List<ProductRankingDto.ProductRankingInfo> {
        val endDate = request.rankingDate
        val startDate = request.period.getStartDate(endDate)

        return productRankingRepository.findTopProducts(startDate, endDate)
            .map { ProductRankingDto.ProductRankingInfo.from(it) }
    }
}
