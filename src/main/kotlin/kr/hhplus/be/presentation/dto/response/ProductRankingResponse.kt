package kr.hhplus.be.presentation.dto.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.application.product.ProductRankingDto
import java.time.LocalDate

@Schema(description = "상품 랭킹 정보")
data class ProductRankingResponse(
    @field:Schema(description = "상품 ID", example = "1")
    val id: Long,

    @field:Schema(description = "상품명", example = "아이폰 15")
    val productName: String,

    @field:Schema(description = "총 판매량", example = "150")
    val totalSalesCount: Int,

    @field:Schema(description = "랭킹", example = "1")
    val rank: Int,

    @field:Schema(description = "랭킹 기준 날짜", example = "2025-08-15")
    val rankingDate: LocalDate
) {
    companion object {
        fun from(dto: ProductRankingDto.ProductRankingInfo): ProductRankingResponse {
            return ProductRankingResponse(
                id = dto.id,
                productName = dto.productName,
                totalSalesCount = dto.totalSalesCount,
                rank = dto.rank,
                rankingDate = dto.rankingDate
            )
        }
    }
}
