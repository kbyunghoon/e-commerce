package kr.hhplus.be.presentation.dto.common

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import kr.hhplus.be.domain.exception.ErrorCode

@Schema(description = "공통 응답 형식")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseResponse<T>(
    @field:Schema(description = "요청 성공 여부", example = "true")
    val success: Boolean,
    
    @field:Schema(description = "응답 데이터 (성공 시)")
    val data: T? = null,
    
    @field:Schema(description = "에러 정보 (실패 시)")
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T? = null): BaseResponse<T> {
            return BaseResponse(success = true, data = data)
        }

        fun <T> error(errorCode: ErrorCode): BaseResponse<T> {
            return BaseResponse(success = false, error = ErrorResponse(errorCode))
        }
        
        fun <T> error(code: String, message: String): BaseResponse<T> {
            return BaseResponse(success = false, error = ErrorResponse(code, message))
        }
    }
}
