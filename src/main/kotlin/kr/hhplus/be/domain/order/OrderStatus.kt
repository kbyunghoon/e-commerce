package kr.hhplus.be.domain.order

enum class OrderStatus(val value: String) {
    PENDING("결제 대기"),
    COMPLETED("완료"),
    CANCELLED("취소됨")
}
