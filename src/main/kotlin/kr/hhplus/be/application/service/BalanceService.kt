package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceInfo
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.BalanceHistoryRepository
import kr.hhplus.be.domain.user.TransactionType
import kr.hhplus.be.domain.user.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BalanceService(
    private val userRepository: UserRepository,
    private val balanceHistoryRepository: BalanceHistoryRepository
) {

    fun charge(command: BalanceChargeCommand): BalanceInfo {
        if (command.amount <= 0) {
            throw BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)
        }

        val user = userRepository.findById(command.userId) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        user.chargeBalance(command.amount)
        val updatedUser = userRepository.save(user)

        return BalanceInfo.from(updatedUser)
    }

    fun use(command: BalanceDeductCommand): BalanceInfo {
        val user = userRepository.findById(command.userId) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        user.deductBalance(command.amount)
        val updatedUser = userRepository.save(user)

        return BalanceInfo.from(updatedUser)
    }

    fun getBalance(userId: Long): BalanceInfo {
        val user = userRepository.findById(userId) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        return BalanceInfo.from(user)
    }

    fun recordChargeHistory(userId: Long, currentAmount: Int, chargeAmount: Int) {
        val history = BalanceHistory(
            userId = userId,
            amount = chargeAmount,
            beforeAmount = currentAmount,
            afterAmount = currentAmount + chargeAmount,
            type = TransactionType.CHARGE,
            transactionAt = LocalDateTime.now()
        )
        balanceHistoryRepository.save(history)
    }

    fun recordDeductHistory(userId: Long, currentAmount: Int, deductAmount: Int) {
        val history = BalanceHistory(
            userId = userId,
            amount = deductAmount,
            beforeAmount = currentAmount,
            afterAmount = currentAmount - deductAmount,
            type = TransactionType.DEDUCT,
            transactionAt = LocalDateTime.now()
        )
        balanceHistoryRepository.save(history)
    }

    fun refund(userId: Long, amount: Int): BalanceInfo {
        val user = userRepository.findById(userId) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
        val currentAmount = user.balance
        user.chargeBalance(amount)
        val updatedUser = userRepository.save(user)
        recordRefundHistory(userId, currentAmount, amount)
        return BalanceInfo.from(updatedUser)
    }

    fun recordRefundHistory(userId: Long, currentAmount: Int, refundAmount: Int) {
        val history = BalanceHistory(
            userId = userId,
            amount = refundAmount,
            beforeAmount = currentAmount,
            afterAmount = currentAmount + refundAmount,
            type = TransactionType.REFUND,
            transactionAt = LocalDateTime.now()
        )
        balanceHistoryRepository.save(history)
    }
}
