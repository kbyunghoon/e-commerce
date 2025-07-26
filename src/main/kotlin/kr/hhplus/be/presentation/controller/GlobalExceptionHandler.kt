package kr.hhplus.be.presentation.controller

import io.swagger.v3.oas.annotations.Hidden
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.presentation.dto.common.BaseResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(e.errorCode.status)
            .body(BaseResponse.error(e.errorCode))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Any>> {
        val fieldError = e.bindingResult.fieldErrors.firstOrNull()
        val errorMessage = fieldError?.defaultMessage ?: "입력값이 유효하지 않습니다"
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error(ErrorCode.INVALID_INPUT_VALUE.code, errorMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(ErrorCode.UNKNOWN_ERROR.status)
            .body(BaseResponse.error(ErrorCode.UNKNOWN_ERROR))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameter(e: MissingServletRequestParameterException): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error(ErrorCode.INVALID_INPUT_VALUE))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error(ErrorCode.INVALID_INPUT_VALUE))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(e: HttpMessageNotReadableException): ResponseEntity<BaseResponse<Any>> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error(ErrorCode.INVALID_INPUT_VALUE))
    }
}
