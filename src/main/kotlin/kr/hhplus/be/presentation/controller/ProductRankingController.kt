package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.PastOrPresent
import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.domain.product.RankingPeriod
import kr.hhplus.be.presentation.api.ProductRankingApi
import kr.hhplus.be.presentation.dto.common.BaseResponse
import kr.hhplus.be.presentation.dto.response.ProductRankingListResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/v1/products/top")
class ProductRankingController(
    private val productRankingService: ProductRankingService
) : ProductRankingApi {

    @GetMapping
    override fun getTopProducts(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @PastOrPresent(message = "랭킹 날짜는 미래 날짜일 수 없습니다")
        rankingDate: LocalDate?,
    ): BaseResponse<ProductRankingListResponse> {
        val command = ProductRankingCommand(
            rankingDate = rankingDate ?: LocalDate.now(),
            period = RankingPeriod.THREE_DAYS
        )
        val response = productRankingService.getTopProducts(command)

        return BaseResponse.success(ProductRankingListResponse.from(response))
    }
}
