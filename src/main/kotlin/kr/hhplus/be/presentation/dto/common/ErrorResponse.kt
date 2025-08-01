package kr.hhplus.be.presentation.dto.common

import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.exception.ErrorCode

@Schema(description = "에러 응답")
data class ErrorResponse(
    @field:Schema(description = "에러 코드", example = "INVALID_INPUT_VALUE")
    val code: String,
    
    @field:Schema(description = "에러 메시지", example = "잘못된 값을 입력했습니다.")
    val message: String
) {
    constructor(errorCode: ErrorCode) : this(errorCode.name, errorCode.message)
}
