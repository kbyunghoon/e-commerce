package kr.hhplus.be.presentation.controller

import io.swagger.v3.oas.annotations.Hidden
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.presentation.dto.common.BaseResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@Hidden
@RestControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<BaseResponse<Any>> {
        log.warn("BusinessException occurred: {} - {}", e.errorCode.name, e.message)
        return ResponseEntity
            .status(e.errorCode.status)
            .body(BaseResponse.error(e.errorCode))
    }

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val fieldError = ex.bindingResult.fieldErrors.firstOrNull()
        val errorMessage = fieldError?.defaultMessage ?: "입력값이 유효하지 않습니다"
        log.warn("Validation failed: {}", errorMessage, ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error<Any>(ErrorCode.INVALID_INPUT_VALUE.name, errorMessage))
    }

    override fun handleMissingServletRequestParameter(
        ex: MissingServletRequestParameterException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorMessage = "필수 파라미터가 누락되었습니다: ${ex.parameterName}"
        log.warn("Missing request parameter: {}", ex.parameterName, ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error<Any>(ErrorCode.INVALID_INPUT_VALUE.name, errorMessage))
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorMessage = "요청 본문을 읽을 수 없습니다. JSON 형식을 확인해주세요"
        log.warn("Unreadable HTTP message", ex)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error<Any>(ErrorCode.INVALID_INPUT_VALUE.name, errorMessage))
    }

    override fun handleHttpRequestMethodNotSupported(
        ex: HttpRequestMethodNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorMessage = "지원하지 않는 HTTP 메서드입니다: ${ex.method}"
        log.warn("Method not supported: {}", ex.method, ex)

        return ResponseEntity
            .status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(BaseResponse.error<Any>(ErrorCode.METHOD_NOT_ALLOWED.name, errorMessage))
    }

    override fun handleHttpMediaTypeNotSupported(
        ex: HttpMediaTypeNotSupportedException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest
    ): ResponseEntity<Any>? {
        val errorMessage = "지원하지 않는 미디어 타입입니다: ${ex.contentType}"
        log.warn("Media type not supported: {}", ex.contentType, ex)

        return ResponseEntity
            .status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(BaseResponse.error<Any>(ErrorCode.UNSUPPORTED_MEDIA_TYPE.name, errorMessage))
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatch(e: MethodArgumentTypeMismatchException): ResponseEntity<BaseResponse<Any>> {
        val errorMessage = "파라미터 타입이 올바르지 않습니다: ${e.name}"
        log.warn("Argument type mismatch: {}", e.name, e)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error<Any>(ErrorCode.INVALID_INPUT_VALUE.name, errorMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneralException(e: Exception): ResponseEntity<BaseResponse<Any>> {
        log.error("Unhandled exception occurred", e)

        return ResponseEntity
            .status(ErrorCode.UNKNOWN_ERROR.status)
            .body(BaseResponse.error(ErrorCode.UNKNOWN_ERROR))
    }
}