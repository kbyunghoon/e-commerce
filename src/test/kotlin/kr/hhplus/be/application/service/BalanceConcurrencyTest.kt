package kr.hhplus.be.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import org.springframework.test.context.TestConstructor
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicLong

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class BalanceConcurrencyTest(
    private val balanceService: BalanceService,
    private val userRepository: UserRepository
) : DescribeSpec({

    describe("잔액 충전 동시성 테스트") {

        it("동시에 여러 요청이 들어와도 잔액이 정확하게 충전되어야 한다") {
            // Given
            val userId = 1L
            val initialBalance = 0
            val chargeAmount = 1000
            val threadCount = 100

            val user = User(id = userId, balance = initialBalance, name = "testUser", email = "test@example.com")
            userRepository.save(user)

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicLong(0)

            // When
            repeat(threadCount) {
                executor.submit {
                    try {
                        val command = BalanceChargeCommand(userId, chargeAmount)
                        balanceService.charge(command)
                        successCount.incrementAndGet()
                    } catch (e: Exception) {
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            val finalUser = userRepository.findByIdOrThrow(userId)
            finalUser.balance shouldBe (initialBalance + (chargeAmount * successCount.get()))
            successCount.get() shouldBe threadCount.toLong()
        }
    }

    describe("잔액 사용 동시성 테스트") {

        it("동시에 여러 요청이 들어와도 잔액이 정확하게 사용되어야 한다") {
            // Given
            val userId = 2L
            val initialBalance = 100000
            val useAmount = 1000
            val threadCount = 50

            val user = User(id = userId, balance = initialBalance, name = "testUser", email = "test@example.com")
            userRepository.save(user)

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicLong(0)
            val failureCount = AtomicLong(0)

            // When
            repeat(threadCount) {
                executor.submit {
                    try {
                        val command = BalanceDeductCommand(userId, useAmount)
                        balanceService.use(command)
                        successCount.incrementAndGet()
                    } catch (e: BusinessException) {
                        if (e.errorCode == ErrorCode.INSUFFICIENT_BALANCE) {
                            failureCount.incrementAndGet()
                        } else {
                            throw e
                        }
                    } catch (e: Exception) {
                        failureCount.incrementAndGet()
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            val finalUser = userRepository.findByIdOrThrow(userId)
            val expectedFinalBalance = initialBalance - (useAmount * successCount.get())
            finalUser.balance shouldBe expectedFinalBalance
            (successCount.get() + failureCount.get()) shouldBe threadCount.toLong()
        }

        it("잔액이 부족할 때 동시에 여러 사용 요청이 들어오면, 잔액 범위 내에서만 사용되어야 한다") {
            // Given
            val userId = 3L
            val initialBalance = 5000
            val useAmount = 1000
            val threadCount = 10

            val user = User(id = userId, balance = initialBalance, name = "testUser", email = "test@example.com")
            userRepository.save(user)

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicLong(0)
            val failureCount = AtomicLong(0)

            // When
            repeat(threadCount) {
                executor.submit {
                    try {
                        val command = BalanceDeductCommand(userId, useAmount)
                        balanceService.use(command)
                        successCount.incrementAndGet()
                    } catch (e: BusinessException) {
                        if (e.errorCode == ErrorCode.INSUFFICIENT_BALANCE) {
                            failureCount.incrementAndGet()
                        } else {
                            throw e
                        }
                    } catch (e: Exception) {
                        failureCount.incrementAndGet()
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            val finalUser = userRepository.findByIdOrThrow(userId)
            finalUser.balance shouldBe (initialBalance - (useAmount * successCount.get()))
            successCount.get() shouldBe (initialBalance / useAmount)
            (successCount.get() + failureCount.get()) shouldBe threadCount.toLong()
        }
    }
})
