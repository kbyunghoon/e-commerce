package kr.hhplus.be.domain.coupon

enum class CouponIssueResult(val value: String) {
    SUCCESS("SUCCESS"),
    ALREADY_ISSUED("ALREADY_ISSUED"),
    SOLD_OUT("SOLD_OUT"),
    UNKNOWN_ERROR("UNKNOWN_ERROR")
}