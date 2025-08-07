package kr.hhplus.be.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.support.concurrent.ConcurrentTestExecutor
import kr.hhplus.be.support.concurrent.ConcurrentTestResult
import org.springframework.test.context.TestConstructor

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BalanceConcurrencyTest(
    private val balanceService: BalanceService,
    private val userRepository: UserRepository
) : DescribeSpec({

    val executor = ConcurrentTestExecutor()

    describe("포인트 충전 동시성 테스트") {

        it("유저 포인트 충전 2번 동시성 테스트") {
            // given
            val user = User(
                id = 0,
                balance = 0,
                name = "충전테스트유저1",
                email = "charge1@example.com"
            )
            val savedUser = userRepository.save(user)
            val command = BalanceChargeCommand(userId = savedUser.id, amount = 1000)

            // when
            val result: ConcurrentTestResult = executor.execute(2, 2) {
                balanceService.charge(command)
            }

            // then
            println("=== 유저 포인트 충전 2번 동시성 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            result.getExceptions().forEach { exception ->
                println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
            }

            val updatedUser = userRepository.findByIdOrThrow(savedUser.id)
            println("최종 유저 잔액: ${updatedUser.balance}")

            val expectedBalance = result.getSuccessCount().get() * 1000
            updatedUser.balance shouldBe expectedBalance
        }

        it("유저 포인트 충전 10번 동시성 테스트") {
            // given
            val user = User(
                id = 0,
                balance = 0,
                name = "충전테스트유저2",
                email = "charge2@example.com"
            )
            val savedUser = userRepository.save(user)
            val command = BalanceChargeCommand(userId = savedUser.id, amount = 100)

            // when
            val result: ConcurrentTestResult = executor.execute(5, 10) {
                balanceService.charge(command)
            }

            // then
            println("=== 유저 포인트 충전 10번 동시성 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            result.getExceptions().forEach { exception ->
                println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
            }

            val updatedUser = userRepository.findByIdOrThrow(savedUser.id)
            println("최종 유저 잔액: ${updatedUser.balance}")

            val expectedBalance = result.getSuccessCount().get() * 100
            updatedUser.balance shouldBe expectedBalance
        }

        it("대량 포인트 충전 동시성 테스트") {
            // given
            val user = User(
                id = 0,
                balance = 0,
                name = "대량충전테스트유저",
                email = "bulk@example.com"
            )
            val savedUser = userRepository.save(user)
            val command = BalanceChargeCommand(userId = savedUser.id, amount = 1)
            val threadCount = 50
            val taskCount = 100

            // when
            val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
                balanceService.charge(command)
            }

            // then
            println("=== 대량 포인트 충전 동시성 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")
            println("성공률: ${(result.getSuccessCount().get().toDouble() / taskCount * 100).toInt()}%")

            result.getExceptions().forEach { exception ->
                println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
            }

            val updatedUser = userRepository.findByIdOrThrow(savedUser.id)
            println("최종 유저 잔액: ${updatedUser.balance}")

            updatedUser.balance shouldBe result.getSuccessCount().get()
        }
    }

    describe("포인트 차감 동시성 테스트") {

        it("유저 포인트 차감 동시성 테스트") {
            // given
            val user = User(
                id = 0,
                balance = 0,
                name = "차감테스트유저1",
                email = "deduct1@example.com"
            )
            val savedUser = userRepository.save(user)

            val chargeCommand = BalanceChargeCommand(userId = savedUser.id, amount = 5000)
            balanceService.charge(chargeCommand)

            val deductCommand = BalanceDeductCommand(userId = savedUser.id, amount = 100)

            // when
            val result: ConcurrentTestResult = executor.execute(5, 10) {
                balanceService.use(deductCommand)
            }

            // then
            println("=== 유저 포인트 차감 동시성 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            result.getExceptions().forEach { exception ->
                println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
            }

            val updatedUser = userRepository.findByIdOrThrow(savedUser.id)
            println("최종 유저 잔액: ${updatedUser.balance}")

            val expectedBalance = 5000 - (result.getSuccessCount().get() * 100)
            updatedUser.balance shouldBe expectedBalance
            updatedUser.balance shouldBeGreaterThanOrEqualTo 0
        }

        it("잔액 부족 상황에서 동시 차감 테스트") {
            // given
            val user = User(
                id = 0,
                balance = 0,
                name = "차감테스트유저2",
                email = "deduct2@example.com"
            )
            val savedUser = userRepository.save(user)

            val chargeCommand = BalanceChargeCommand(userId = savedUser.id, amount = 500)
            balanceService.charge(chargeCommand)

            val deductCommand = BalanceDeductCommand(userId = savedUser.id, amount = 100)

            // when
            val result: ConcurrentTestResult = executor.execute(10, 20) {
                balanceService.use(deductCommand)
            }

            // then
            println("=== 잔액 부족 상황에서 동시 차감 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            result.getExceptions().forEach { exception ->
                println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
            }

            val updatedUser = userRepository.findByIdOrThrow(savedUser.id)
            println("최종 유저 잔액: ${updatedUser.balance}")

            result.getSuccessCount().get() shouldBeLessThanOrEqualTo 5
            updatedUser.balance shouldBeGreaterThanOrEqualTo 0

            val expectedBalance = 500 - (result.getSuccessCount().get() * 100)
            updatedUser.balance shouldBe expectedBalance
        }
    }
})
