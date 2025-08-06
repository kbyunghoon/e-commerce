package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceDto.BalanceInfo
import kr.hhplus.be.application.balance.BalanceRefundCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.domain.user.events.BalanceChargedEvent
import kr.hhplus.be.domain.user.events.BalanceDeductedEvent
import kr.hhplus.be.domain.user.events.BalanceRefundedEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceService(
    private val userRepository: UserRepository,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
    fun charge(command: BalanceChargeCommand): BalanceInfo {
        if (command.amount <= 0) {
            throw BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)
        }

        val user = userRepository.findByIdOrThrow(command.userId)
        val beforeAmount = user.balance

        user.chargeBalance(command.amount)
        val updatedUser = userRepository.save(user)

        applicationEventPublisher.publishEvent(
            BalanceChargedEvent(
                _userId = updatedUser.id,
                _beforeAmount = beforeAmount,
                _afterAmount = updatedUser.balance,
                _chargedAmount = command.amount
            )
        )

        return BalanceInfo.from(updatedUser)
    }

    @Transactional
    fun use(command: BalanceDeductCommand): BalanceInfo {
        val user = userRepository.findByIdOrThrow(command.userId)

        val beforeAmount = user.balance

        user.deductBalance(command.amount)
        val updatedUser = userRepository.save(user)

        applicationEventPublisher.publishEvent(
            BalanceDeductedEvent(
                _userId = updatedUser.id,
                _beforeAmount = beforeAmount,
                _afterAmount = updatedUser.balance,
                _deductedAmount = command.amount
            )
        )

        return BalanceInfo.from(updatedUser)
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: Long): BalanceInfo {
        val user = userRepository.findByIdOrThrow(userId)
        return BalanceInfo.from(user)
    }

    @Transactional
    fun refund(command: BalanceRefundCommand): BalanceInfo {
        val user = userRepository.findByIdOrThrow(command.userId)
        val beforeAmount = user.balance

        user.chargeBalance(command.amount)
        val updatedUser = userRepository.save(user)

        applicationEventPublisher.publishEvent(
            BalanceRefundedEvent(
                _userId = updatedUser.id,
                _beforeAmount = beforeAmount,
                _afterAmount = updatedUser.balance,
                _refundedAmount = command.amount
            )
        )

        return BalanceInfo.from(updatedUser)
    }
}
