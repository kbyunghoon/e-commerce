package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.config.TestContainerConfig
import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.support.concurrent.ConcurrentTestExecutor
import kr.hhplus.be.support.concurrent.ConcurrentTestResult
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.assertj.core.api.Assertions.assertThat
import java.util.concurrent.Executors

@SpringBootTest
@ActiveProfiles("test")
@Import(TestContainerConfig::class)
class BalanceConcurrencyTest {

    @Autowired
    private lateinit var balanceService: BalanceService

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var user: User
    private lateinit var executor: ConcurrentTestExecutor

    @BeforeEach
    fun setUp() {
        user = User(
            id = 0,
            balance = 0,
            name = "테스트유저",
            email = "test@example.com"
        )
        user = userRepository.save(user)
        executor = ConcurrentTestExecutor()
    }

    @Test
    fun `유저 포인트 충전 2번 동시성 테스트`() {
        // given
        val command = BalanceChargeCommand(userId = user.id, amount = 1000)

        // when
        val result: ConcurrentTestResult = executor.execute(2, 2) {
            balanceService.charge(command)
        }

        // then
        println("성공 카운트: ${result.getSuccessCount().get()}")
        println("실패 카운트: ${result.getFailureCount().get()}")
        
        result.getExceptions().forEach { exception ->
            println("예외: ${exception.message}")
        }

        val updatedUser = userRepository.findByIdOrThrow(user.id)
        println("최종 유저 잔액: ${updatedUser.balance}")
        
        val expectedBalance = result.getSuccessCount().get() * 1000
        assertThat(updatedUser.balance).isEqualTo(expectedBalance)
    }

    @Test
    fun `유저 포인트 충전 10번 동시성 테스트`() {
        // given
        val command = BalanceChargeCommand(userId = user.id, amount = 100)

        // when
        val result: ConcurrentTestResult = executor.execute(5, 10) {
            balanceService.charge(command)
        }

        // then
        println("성공 카운트: ${result.getSuccessCount().get()}")
        println("실패 카운트: ${result.getFailureCount().get()}")
        
        result.getExceptions().forEach { exception ->
            println("예외: ${exception.message}")
        }

        val updatedUser = userRepository.findByIdOrThrow(user.id)
        println("최종 유저 잔액: ${updatedUser.balance}")
        
        val expectedBalance = result.getSuccessCount().get() * 100
        assertThat(updatedUser.balance).isEqualTo(expectedBalance)
    }

    @Test
    fun `유저 포인트 차감 동시성 테스트`() {
        // given
        val chargeCommand = BalanceChargeCommand(userId = user.id, amount = 5000)
        balanceService.charge(chargeCommand)

        val deductCommand = BalanceDeductCommand(userId = user.id, amount = 100)

        // when
        val result: ConcurrentTestResult = executor.execute(5, 10) {
            balanceService.use(deductCommand)
        }

        // then
        println("성공 카운트: ${result.getSuccessCount().get()}")
        println("실패 카운트: ${result.getFailureCount().get()}")
        
        result.getExceptions().forEach { exception ->
            println("예외: ${exception.message}")
        }

        val updatedUser = userRepository.findByIdOrThrow(user.id)
        println("최종 유저 잔액: ${updatedUser.balance}")
        
        // 성공한 차감 횟수만큼 잔액이 차감되어야 함
        val expectedBalance = 5000 - (result.getSuccessCount().get() * 100)
        assertThat(updatedUser.balance).isEqualTo(expectedBalance)
        assertThat(updatedUser.balance).isGreaterThanOrEqualTo(0)
    }

    @Test
    fun `잔액 부족 상황에서 동시 차감 테스트`() {
        // given
        // 초기 잔액을 적게 설정
        val chargeCommand = BalanceChargeCommand(userId = user.id, amount = 500)
        balanceService.charge(chargeCommand)

        val deductCommand = BalanceDeductCommand(userId = user.id, amount = 100)

        // when
        val result: ConcurrentTestResult = executor.execute(10, 20) {
            balanceService.use(deductCommand)
        }

        // then
        println("성공 카운트: ${result.getSuccessCount().get()}")
        println("실패 카운트: ${result.getFailureCount().get()}")
        
        result.getExceptions().forEach { exception ->
            println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
        }

        val updatedUser = userRepository.findByIdOrThrow(user.id)
        println("최종 유저 잔액: ${updatedUser.balance}")
        
        // 최대 5번까지만 성공할 수 있어야 함 (500 / 100)
        assertThat(result.getSuccessCount().get()).isLessThanOrEqualTo(5)
        assertThat(updatedUser.balance).isGreaterThanOrEqualTo(0)
        
        // 성공한 차감 횟수만큼 정확히 차감되어야 함
        val expectedBalance = 500 - (result.getSuccessCount().get() * 100)
        assertThat(updatedUser.balance).isEqualTo(expectedBalance)
    }

    @Test
    fun `대량 포인트 충전 동시성 테스트`() {
        // given
        val command = BalanceChargeCommand(userId = user.id, amount = 1)
        val threadCount = 50
        val taskCount = 100

        // when
        val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
            balanceService.charge(command)
        }

        // then
        println("성공 카운트: ${result.getSuccessCount().get()}")
        println("실패 카운트: ${result.getFailureCount().get()}")
        
        result.getExceptions().forEach { exception ->
            println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
        }

        val updatedUser = userRepository.findByIdOrThrow(user.id)
        println("최종 유저 잔액: ${updatedUser.balance}")
        
        // 성공한 만큼 정확히 충전되어야 함
        assertThat(updatedUser.balance).isEqualTo(result.getSuccessCount().get())
    }
}
