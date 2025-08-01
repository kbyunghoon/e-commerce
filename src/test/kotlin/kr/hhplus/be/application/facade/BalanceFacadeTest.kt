package kr.hhplus.be.application.facade

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.balance.BalanceChargeCommand
import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceDto.BalanceInfo
import kr.hhplus.be.application.service.BalanceService
import java.time.LocalDateTime

class BalanceFacadeTest : BehaviorSpec({
    val balanceService: BalanceService = mockk()
    val balanceFacade = BalanceFacade(balanceService)

    afterContainer {
        clearAllMocks()
    }

    Given("잔액 충전(chargeBalance) 시나리오") {
        val userId = 1L
        val chargeAmount = 10000
        val initialBalance = 5000
        val chargedBalance = initialBalance + chargeAmount
        val now = LocalDateTime.now()

        When("사용자가 잔액 충전을 요청하면") {
            val request = BalanceChargeCommand(userId, chargeAmount)
            val initialBalanceInfo = BalanceInfo(userId, userId, initialBalance, now, now)
            val chargedBalanceInfo = BalanceInfo(userId, userId, chargedBalance, now, now)

            every { balanceService.getBalance(userId) } returns initialBalanceInfo andThen chargedBalanceInfo
            every { balanceService.charge(request) } returns chargedBalanceInfo
            every { balanceService.recordChargeHistory(userId, initialBalance, chargeAmount) } returns Unit

            val response = balanceFacade.chargeBalance(request)

            Then("잔액이 충전되고, 충전 내역이 기록되며, 충전 후 잔액 정보가 반환된다") {
                response.userId shouldBe userId
                response.balance shouldBe chargedBalance
                response.chargedAmount shouldBe chargeAmount

                verify { balanceService.getBalance(userId) }
                verify { balanceService.charge(request) }
                verify { balanceService.recordChargeHistory(userId, initialBalance, chargeAmount) }
            }
        }
    }

    Given("잔액 차감(deductBalance) 시나리오") {
        val userId = 1L
        val deductAmount = 5000
        val initialBalance = 10000
        val deductedBalance = initialBalance - deductAmount
        val now = LocalDateTime.now()

        When("사용자가 잔액 차감을 요청하면") {
            val request = BalanceDeductCommand(userId, deductAmount)
            val initialBalanceInfo = BalanceInfo(userId, userId, initialBalance, now, now)
            val deductedBalanceInfo = BalanceInfo(userId, userId, deductedBalance, now, now)

            every { balanceService.getBalance(userId) } returns initialBalanceInfo andThen deductedBalanceInfo
            every { balanceService.use(request) } returns deductedBalanceInfo
            every { balanceService.recordDeductHistory(userId, initialBalance, deductAmount) } returns Unit

            val response = balanceFacade.deductBalance(request)

            Then("잔액이 차감되고, 차감 내역이 기록되며, 차감 후 잔액 정보가 반환된다") {
                response.userId shouldBe userId
                response.amount shouldBe deductedBalance

                verify { balanceService.getBalance(userId) }
                verify { balanceService.use(request) }
                verify { balanceService.recordDeductHistory(userId, initialBalance, deductAmount) }
            }
        }
    }
})
