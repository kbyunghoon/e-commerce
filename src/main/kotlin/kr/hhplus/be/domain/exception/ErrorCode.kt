package kr.hhplus.be.domain.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val code: String, val message: String) {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 값을 입력했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", "알 수 없는 오류가 발생했습니다."),

    // 잔액
    CHARGE_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "CHARGE_INVALID_AMOUNT", "충전 금액은 0보다 커야 합니다."),
    DEDUCT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "DEDUCT_INVALID_AMOUNT", "차감 금액은 0보다 커야 합니다."),
    CHARGE_FAILED(HttpStatus.BAD_REQUEST, "CHARGE_FAILED", "잔액 충전에 실패했습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_BALANCE", "잔액이 부족합니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),

    // 쿠폰
    COUPON_ISSUE_FAILED(HttpStatus.BAD_REQUEST, "COUPON_ISSUE_FAILED", "쿠폰 발급에 실패했습니다."),
    COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST, "COUPON_SOLD_OUT", "쿠폰이 모두 소진되었습니다."),
    COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "COUPON_NOT_FOUND", "쿠폰을 찾을 수 없습니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "COUPON_EXPIRED", "쿠폰 기간이 만료되었습니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "COUPON_ALREADY_ISSUED", "이미 발급받은 쿠폰입니다."),
    COUPON_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "COUPON_NOT_AVAILABLE", "사용할 수 없는 쿠폰입니다."),
    COUPON_NOT_USED(HttpStatus.BAD_REQUEST, "COUPON_NOT_USED", "사용되지 않은 쿠폰입니다."),
    COUPON_OWNERSHIP_MISMATCH(HttpStatus.BAD_REQUEST, "COUPON_OWNERSHIP_MISMATCH", "보유하지 않은 쿠폰입니다."),
    USER_COUPON_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_COUPON_NOT_FOUND", "사용자 쿠폰이 존재하지 않습니다."),
    
    // 쿠폰 비즈니스 정책 검증
    INVALID_COUPON_NAME(HttpStatus.BAD_REQUEST, "INVALID_COUPON_NAME", "쿠폰명이 유효하지 않습니다."),
    INVALID_DISCOUNT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_DISCOUNT_VALUE", "할인 값이 유효하지 않습니다."),
    INVALID_PERCENTAGE_DISCOUNT(HttpStatus.BAD_REQUEST, "INVALID_PERCENTAGE_DISCOUNT", "퍼센트 할인 값이 유효하지 않습니다."),
    INVALID_FIXED_DISCOUNT(HttpStatus.BAD_REQUEST, "INVALID_FIXED_DISCOUNT", "고정 할인 금액이 유효하지 않습니다."),
    INVALID_EXPIRY_DATE(HttpStatus.BAD_REQUEST, "INVALID_EXPIRY_DATE", "쿠폰 만료일이 유효하지 않습니다."),
    INVALID_TOTAL_QUANTITY(HttpStatus.BAD_REQUEST, "INVALID_TOTAL_QUANTITY", "총 발급 수량이 유효하지 않습니다."),
    COUPON_ISSUE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "COUPON_ISSUE_LIMIT_EXCEEDED", "쿠폰 발급 한도를 초과했습니다."),
    INVALID_COUPON_STATE(HttpStatus.BAD_REQUEST, "INVALID_COUPON_STATE", "쿠폰 상태가 유효하지 않습니다."),
    INVALID_DISCOUNT_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_DISCOUNT_AMOUNT", "할인 계산 대상 금액이 유효하지 않습니다."),

    // 주문
    ORDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "ORDER_NOT_FOUND", "주문 정보를 찾을 수 없습니다."),
    ORDER_ID_NOT_FOUND(HttpStatus.BAD_REQUEST, "ORDER_ID_NOT_FOUND", "주문 ID를 찾을 수 없습니다."),
    ORDER_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "ORDER_ALREADY_PROCESSED", "이미 진행 중인 주문입니다."),
    ORDER_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "ORDER_ALREADY_CANCELLED", "이미 취소 된 주문입니다."),
    ORDER_ITEMS_CANNOT_BE_EMPTY(HttpStatus.BAD_REQUEST, "ORDER_ITEMS_CANNOT_BE_EMPTY", "주문 항목이 비어 있습니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", "재고가 부족합니다."),
    
    // 주문 비즈니스 정책 검증
    INVALID_USER_ID(HttpStatus.BAD_REQUEST, "INVALID_USER_ID", "사용자 ID가 유효하지 않습니다."),
    INVALID_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_ORDER_AMOUNT", "주문 금액이 유효하지 않습니다."),
    DISCOUNT_EXCEEDS_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "DISCOUNT_EXCEEDS_ORDER_AMOUNT", "할인 금액이 주문 금액을 초과합니다."),
    INVALID_FINAL_AMOUNT(HttpStatus.BAD_REQUEST, "INVALID_FINAL_AMOUNT", "최종 결제 금액이 유효하지 않습니다."),
    INVALID_AMOUNT_CALCULATION(HttpStatus.BAD_REQUEST, "INVALID_AMOUNT_CALCULATION", "금액 계산이 올바르지 않습니다."),
    DISCOUNT_RATE_EXCEEDED(HttpStatus.BAD_REQUEST, "DISCOUNT_RATE_EXCEEDED", "할인율이 허용 범위를 초과했습니다."),
    ORDER_STATE_CHANGE_FAILED(HttpStatus.BAD_REQUEST, "ORDER_STATE_CHANGE_FAILED", "주문 상태 변경에 실패했습니다."),
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "INVALID_ORDER_QUANTITY", "주문 수량이 유효하지 않습니다."),
    INVALID_PRODUCT_ID(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_ID", "상품 ID가 유효하지 않습니다."),
    INVALID_PRODUCT_PRICE(HttpStatus.BAD_REQUEST, "INVALID_PRODUCT_PRICE", "상품 가격이 유효하지 않습니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.BAD_REQUEST, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // etc
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
}
