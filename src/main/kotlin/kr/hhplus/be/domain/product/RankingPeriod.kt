package kr.hhplus.be.domain.product

import java.time.LocalDate

enum class RankingPeriod(val days: Long) {
    DAILY(1),
    THREE_DAYS(3),
    WEEKLY(7),
    MONTHLY(30);

    fun getStartDate(endDate: LocalDate): LocalDate {
        return endDate.minusDays(days - 1)
    }
}
