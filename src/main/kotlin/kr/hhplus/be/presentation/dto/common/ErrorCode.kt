package kr.hhplus.be.presentation.dto.common

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val code: String, val message: String) {
    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "INVALID_INPUT_VALUE", "잘못된 값을 입력했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN_ERROR", "알 수 없는 오류가 발생했습니다."),

    // 잔액
    CHARGE_FAILED(HttpStatus.BAD_REQUEST, "CHARGE_FAILED", "잔액 충전에 실패했습니다."),
    INSUFFICIENT_BALANCE(HttpStatus.PAYMENT_REQUIRED, "INSUFFICIENT_BALANCE", "잔액이 부족합니다."),

    // 상품
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "상품을 찾을 수 없습니다."),

    // 쿠폰
    COUPON_ISSUE_FAILED(HttpStatus.BAD_REQUEST, "COUPON_ISSUE_FAILED", "쿠폰 발급에 실패했습니다."),
    COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST, "COUPON_SOLD_OUT", "쿠폰이 모두 소진되었습니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "COUPON_ALREADY_ISSUED", "이미 발급받은 쿠폰입니다."),

    // 유저
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),

    // etc
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "TOO_MANY_REQUESTS", "요청이 너무 많습니다. 잠시 후 다시 시도해주세요.")
}
