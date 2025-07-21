package kr.hhplus.be.adapter.`in`.web.controller

import io.swagger.v3.oas.annotations.Hidden
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.adapter.`in`.web.dto.common.BaseResponse
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(e.errorCode.status)
            .body(BaseResponse.error(e.errorCode))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(ErrorCode.UNKNOWN_ERROR.status)
            .body(BaseResponse.error(ErrorCode.UNKNOWN_ERROR))
    }
}
