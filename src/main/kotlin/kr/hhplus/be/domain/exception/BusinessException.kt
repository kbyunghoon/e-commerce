package kr.hhplus.be.domain.exception

import kr.hhplus.be.domain.exception.ErrorCode

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)