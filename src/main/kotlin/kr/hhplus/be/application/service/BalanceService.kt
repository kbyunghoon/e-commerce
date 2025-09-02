package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceHistoryCommand
import kr.hhplus.be.application.balance.BalanceRefundCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.BalanceHistory
import kr.hhplus.be.domain.user.TransactionType
import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.global.lock.DistributedLock
import kr.hhplus.be.global.lock.LockResource
import kr.hhplus.be.global.lock.LockStrategy
import kr.hhplus.be.global.lock.UserBalanceLockKeyProvider
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BalanceService(
    private val userRepository: UserRepository,
    private val balanceHistoryService: BalanceHistoryService
) {
    @DistributedLock(
        resource = LockResource.USER_BALANCE,
        keyProvider = "userBalanceLockKeyProvider",
        lockStrategy = LockStrategy.SPIN_LOCK,
        waitTime = 5,
        leaseTime = 10
    )
    @Transactional
    fun charge(
        command: BalanceChargeCommand,
        keyProvider: UserBalanceLockKeyProvider = UserBalanceLockKeyProvider(command.userId)
    ): BalanceHistory {
        if (command.amount <= 0) {
            throw BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)
        }

        val user = userRepository.findByIdOrThrow(command.userId)
        val beforeAmount = user.balance

        val updatedUser = userRepository.save(user.chargeBalance(command.amount))

        val balanceHistory = balanceHistoryService.save(
            BalanceHistoryCommand(
                userId = command.userId,
                amount = command.amount,
                beforeAmount = beforeAmount,
                afterAmount = updatedUser.balance,
                type = TransactionType.CHARGE,
                transactionAt = updatedUser.updatedAt
            )
        )

        return balanceHistory
    }

    @DistributedLock(
        resource = LockResource.USER_BALANCE,
        keyProvider = "userBalanceLockKeyProvider",
        lockStrategy = LockStrategy.SPIN_LOCK,
        waitTime = 5,
        leaseTime = 10
    )
    @Transactional
    fun use(
        command: BalanceDeductCommand,
        keyProvider: UserBalanceLockKeyProvider = UserBalanceLockKeyProvider(command.userId)
    ): User {
        val user = userRepository.findByIdOrThrow(command.userId)

        val beforeAmount = user.balance

        val updatedUser = userRepository.save(user.deductBalance(command.amount))

        val balanceHistory = BalanceHistoryCommand(
            userId = command.userId,
            amount = command.amount,
            beforeAmount = beforeAmount,
            afterAmount = updatedUser.balance,
            type = TransactionType.DEDUCT,
            transactionAt = updatedUser.updatedAt
        )

        balanceHistoryService.save(balanceHistory)

        return updatedUser
    }

    @DistributedLock(
        resource = LockResource.USER_BALANCE,
        keyProvider = "userBalanceLockKeyProvider",
        lockStrategy = LockStrategy.SPIN_LOCK,
        waitTime = 5,
        leaseTime = 10
    )
    @Transactional
    fun refund(
        command: BalanceRefundCommand,
        keyProvider: UserBalanceLockKeyProvider = UserBalanceLockKeyProvider(command.userId)
    ): User {
        val user = userRepository.findByIdOrThrow(command.userId)
        val beforeAmount = user.balance

        val updatedUser = userRepository.save(user.chargeBalance(command.amount))

        val balanceHistory = BalanceHistoryCommand(
            userId = command.userId,
            amount = command.amount,
            beforeAmount = beforeAmount,
            afterAmount = updatedUser.balance,
            type = TransactionType.REFUND,
            transactionAt = updatedUser.updatedAt
        )

        balanceHistoryService.save(balanceHistory)

        return updatedUser
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: Long): User {
        return userRepository.findByIdOrThrow(userId)
    }

    @DistributedLock(
        resource = LockResource.USER_BALANCE,
        keyProvider = "userBalanceLockKeyProvider",
        lockStrategy = LockStrategy.SPIN_LOCK,
        waitTime = 5,
        leaseTime = 10
    )
    @Transactional
    fun deductForSaga(
        userId: Long,
        amount: Int,
        keyProvider: UserBalanceLockKeyProvider = UserBalanceLockKeyProvider(userId)
    ) {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.DEDUCTION_INVALID_AMOUNT)
        }

        val user = userRepository.findByIdOrThrow(userId)

        if (user.balance < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_BALANCE)
        }

        userRepository.save(user.deductBalance(amount))
    }

    @DistributedLock(
        resource = LockResource.USER_BALANCE,
        keyProvider = "userBalanceLockKeyProvider",
        lockStrategy = LockStrategy.SPIN_LOCK,
        waitTime = 5,
        leaseTime = 10
    )
    @Transactional
    fun refundForSaga(
        userId: Long,
        amount: Int,
        keyProvider: UserBalanceLockKeyProvider = UserBalanceLockKeyProvider(userId)
    ) {
        if (amount <= 0) {
            throw BusinessException(ErrorCode.REFUND_INVALID_AMOUNT)
        }

        val user = userRepository.findByIdOrThrow(userId)
        userRepository.save(user.chargeBalance(amount))
    }
}
