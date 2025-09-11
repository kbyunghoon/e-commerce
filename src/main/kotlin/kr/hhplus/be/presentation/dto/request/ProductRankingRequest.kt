package kr.hhplus.be.presentation.dto.request

import jakarta.validation.constraints.PastOrPresent
import kr.hhplus.be.application.product.ProductRankingCommand
import kr.hhplus.be.domain.product.RankingPeriod
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class ProductRankingRequest(
    @field:PastOrPresent(message = "랭킹 날짜는 미래 날짜일 수 없습니다")
    @param:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val rankingDate: LocalDate? = null
) {
    fun toCommand(): ProductRankingCommand {
        return ProductRankingCommand(
            rankingDate = rankingDate ?: LocalDate.now(),
            period = RankingPeriod.THREE_DAYS
        )
    }
}
