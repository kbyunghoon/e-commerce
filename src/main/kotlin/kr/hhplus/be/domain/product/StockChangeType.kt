package kr.hhplus.be.domain.product

enum class StockChangeType(val reason: String) {
    DEDUCT("상품 주문으로 인한 재고 차감"),
    RESTORE("주문 취소로 인한 재고 복원"),
}