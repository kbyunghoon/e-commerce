package kr.hhplus.be.domain.coupon

enum class CouponStatus(val value: String) {
    AVAILABLE("사용가능"),
    USED("사용됨"),
    EXPIRED("만료됨")
}
