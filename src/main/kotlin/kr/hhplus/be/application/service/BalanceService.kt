package kr.hhplus.be.application.service

import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.domain.exception.BusinessException
import org.springframework.stereotype.Service

@Service
class BalanceService {
    fun charge(userId: Long, amount: Int): Boolean {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        return true
    }

    fun getBalance(userId: Long): Int {
        return 20000
    }
}
