package kr.hhplus.be.domain.exception

import kr.hhplus.be.presentation.dto.common.ErrorCode

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)