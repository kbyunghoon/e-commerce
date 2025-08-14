package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.domain.user.events.BalanceChargedEvent
import kr.hhplus.be.domain.user.events.BalanceDeductedEvent
import org.springframework.context.ApplicationEventPublisher

class BalanceServiceTest : BehaviorSpec({
    val userRepository: UserRepository = mockk()
    val applicationEventPublisher: ApplicationEventPublisher = mockk()
    val balanceService = BalanceService(userRepository, applicationEventPublisher)

    afterContainer {
        clearAllMocks()
    }

    Given("잔액 충전(charge) 시나리오") {
        val userId = 1L
        val initialBalance = 10000
        val user = User(id = userId, name = "Test User", email = "test@test.com", balance = initialBalance)

        When("유효한 금액으로 충전을 요청하면") {
            val chargeAmount = 5000
            val command = BalanceChargeCommand(userId, chargeAmount)
            val expectedBalance = initialBalance + chargeAmount

            every { userRepository.findByIdOrThrow(userId) } returns user
            every { userRepository.save(any()) } answers { it.invocation.args[0] as User }
            every { applicationEventPublisher.publishEvent(any<BalanceChargedEvent>()) } returns Unit

            val result = balanceService.charge(command)

            Then("사용자의 잔액이 증가하고, 이벤트가 발행된다") {
                result.userId shouldBe userId
                result.amount shouldBe expectedBalance
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
                verify(exactly = 1) { userRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<BalanceChargedEvent>()) }
            }
        }

        When("0원 이하의 금액으로 충전을 요청하면") {
            val chargeAmount = 0
            val command = BalanceChargeCommand(userId, chargeAmount)

            val exception = shouldThrow<BusinessException> {
                balanceService.charge(command)
            }

            Then("CHARGE_INVALID_AMOUNT 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.CHARGE_INVALID_AMOUNT
                verify(exactly = 0) { userRepository.findByIdOrThrow(any()) }
                verify(exactly = 0) { userRepository.save(any()) }
                verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
            }
        }

        When("존재하지 않는 사용자 ID로 충전을 요청하면") {
            val chargeAmount = 5000
            val command = BalanceChargeCommand(userId, chargeAmount)

            every { userRepository.findByIdOrThrow(userId) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                balanceService.charge(command)
            }

            Then("USER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
                verify(exactly = 0) { userRepository.save(any()) }
                verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
            }
        }
    }

    Given("잔액 사용(use) 시나리오") {
        val userId = 1L
        val initialBalance = 10000
        val user = User(id = userId, name = "Test User", email = "test@test.com", balance = initialBalance)

        When("유효한 금액으로 잔액 사용을 요청하면") {
            val deductAmount = 5000
            val command = BalanceDeductCommand(userId, deductAmount)
            val expectedBalance = initialBalance - deductAmount

            every { userRepository.findByIdOrThrow(userId) } returns user
            every { userRepository.save(any()) } answers { it.invocation.args[0] as User }
            every { applicationEventPublisher.publishEvent(any<BalanceDeductedEvent>()) } returns Unit

            val result = balanceService.use(command)

            Then("사용자의 잔액이 감소하고, 이벤트가 발행된다") {
                result.userId shouldBe userId
                result.amount shouldBe expectedBalance
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
                verify(exactly = 1) { userRepository.save(any()) }
                verify(exactly = 1) { applicationEventPublisher.publishEvent(any<BalanceDeductedEvent>()) }
            }
        }

        When("0원 이하의 금액으로 잔액 사용을 요청하면") {
            val deductAmount = 0
            val command = BalanceDeductCommand(userId, deductAmount)

            every { userRepository.findByIdOrThrow(userId) } returns user
            val exception = shouldThrow<BusinessException> {
                balanceService.use(command)
            }

            Then("DEDUCT_INVALID_AMOUNT 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.DEDUCT_INVALID_AMOUNT
                verify(exactly = 1) { userRepository.findByIdOrThrow(any()) }
                verify(exactly = 0) { userRepository.save(any()) }
                verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
            }
        }

        When("잔액보다 많은 금액으로 잔액 사용을 요청하면") {
            val deductAmount = 15000
            val command = BalanceDeductCommand(userId, deductAmount)

            every { userRepository.findByIdOrThrow(userId) } returns user

            val exception = shouldThrow<BusinessException> {
                balanceService.use(command)
            }

            Then("INSUFFICIENT_BALANCE 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.INSUFFICIENT_BALANCE
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
                verify(exactly = 0) { userRepository.save(any()) }
                verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
            }
        }

        When("존재하지 않는 사용자 ID로 잔액 사용을 요청하면") {
            val deductAmount = 5000
            val command = BalanceDeductCommand(userId, deductAmount)

            every { userRepository.findByIdOrThrow(userId) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                balanceService.use(command)
            }

            Then("USER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
                verify(exactly = 0) { userRepository.save(any()) }
                verify(exactly = 0) { applicationEventPublisher.publishEvent(any()) }
            }
        }
    }

    Given("잔액 조회(getBalance) 시나리오") {
        val userId = 1L
        val balance = 20000
        val user = User(id = userId, name = "Test User", email = "test@test.com", balance = balance)

        When("존재하는 사용자 ID로 잔액 조회를 요청하면") {
            every { userRepository.findByIdOrThrow(userId) } returns user

            val result = balanceService.getBalance(userId)

            Then("사용자의 현재 잔액 정보가 반환된다") {
                result.userId shouldBe userId
                result.amount shouldBe balance
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
            }
        }

        When("존재하지 않는 사용자 ID로 잔액 조회를 요청하면") {
            every { userRepository.findByIdOrThrow(userId) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                balanceService.getBalance(userId)
            }

            Then("USER_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.USER_NOT_FOUND
                verify(exactly = 1) { userRepository.findByIdOrThrow(userId) }
            }
        }
    }
})
