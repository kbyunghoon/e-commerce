package kr.hhplus.be.domain.exception

class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)