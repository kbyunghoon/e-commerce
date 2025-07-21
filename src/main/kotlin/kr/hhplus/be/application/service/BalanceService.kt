package kr.hhplus.be.application.service

import kr.hhplus.be.adapter.out.persistence.entity.BalanceChargeHistoryEntity
import kr.hhplus.be.adapter.out.persistence.entity.UserEntity
import kr.hhplus.be.adapter.out.persistence.repository.BalanceChargeHistoryRepository
import kr.hhplus.be.adapter.out.persistence.repository.UserRepository
import kr.hhplus.be.application.dto.BalanceChargeCommand
import kr.hhplus.be.application.dto.BalanceInfo
import kr.hhplus.be.application.port.`in`.BalanceUseCase
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.model.BalanceChargeHistory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class BalanceService(
    private val userRepository: UserRepository,
    private val balanceChargeHistoryRepository: BalanceChargeHistoryRepository
) : BalanceUseCase {

    @Transactional
    override fun charge(command: BalanceChargeCommand): BalanceInfo {
        if (command.amount <= 0) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }

        val userEntity = userRepository.findByUserId(command.userId)
        val user = userEntity?.toDomain() ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        user.chargeBalance(command.amount)
        val updatedUserEntity = userRepository.save(UserEntity.fromDomain(user))
        val updatedUser = updatedUserEntity.toDomain()

        val history = BalanceChargeHistory(
            userId = updatedUser.userId,
            amount = command.amount,
            chargedAt = LocalDateTime.now()
        )
        balanceChargeHistoryRepository.save(BalanceChargeHistoryEntity.fromDomain(history))

        return BalanceInfo(
            id = updatedUser.userId,
            userId = updatedUser.userId,
            amount = updatedUser.balance,
            createdAt = updatedUser.createdAt,
            updatedAt = updatedUser.updatedAt
        )
    }

    @Transactional(readOnly = true)
    override fun getBalance(userId: Long): BalanceInfo {
        val userEntity = userRepository.findByUserId(userId)
            ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)

        val user = userEntity.toDomain()

        return BalanceInfo(
            id = user.userId,
            userId = user.userId,
            amount = user.balance,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
    }
}
