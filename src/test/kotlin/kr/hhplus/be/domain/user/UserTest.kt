package kr.hhplus.be.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode

class UserTest : FunSpec({

    test("양수 금액으로 충전하면 잔액이 증가한다") {
        // Given
        val user = User(
            id = 1L,
            name = "테스트유저",
            email = "test@test.com",
            balance = 1000
        )

        // When
        user.chargeBalance(5000)

        // Then
        user.balance shouldBe 6000
    }

    test("0원 또는 음수 금액으로 충전하면 예외가 발생한다") {
        // Given
        val user = User(
            id = 1L,
            name = "테스트유저",
            email = "test@test.com",
            balance = 1000
        )

        // Then
        shouldThrow<BusinessException> {
            user.chargeBalance(0)
        }.errorCode shouldBe ErrorCode.CHARGE_INVALID_AMOUNT

        shouldThrow<BusinessException> {
            user.chargeBalance(-1000)
        }.errorCode shouldBe ErrorCode.CHARGE_INVALID_AMOUNT
    }

    test("잔액 범위 내에서 차감하면 잔액이 감소한다") {
        // Given
        val user = User(
            id = 1L,
            name = "테스트유저",
            email = "test@test.com",
            balance = 10000
        )

        // When
        user.deductBalance(3000)

        // Then
        user.balance shouldBe 7000
    }

    test("잔액보다 큰 금액을 차감하려고 하면 예외가 발생한다") {
        // Given
        val user = User(
            id = 1L,
            name = "테스트유저",
            email = "test@test.com",
            balance = 10000
        )

        // Then
        shouldThrow<BusinessException> {
            user.deductBalance(15000)
        }.errorCode shouldBe ErrorCode.INSUFFICIENT_BALANCE
    }

    test("0원 또는 음수 금액으로 차감하려고 하면 예외가 발생한다") {
        // Given
        val user = User(
            id = 1L,
            name = "테스트유저",
            email = "test@test.com",
            balance = 10000
        )

        // Then
        shouldThrow<BusinessException> {
            user.deductBalance(0)
        }.errorCode shouldBe ErrorCode.DEDUCT_INVALID_AMOUNT

        shouldThrow<BusinessException> {
            user.deductBalance(-1000)
        }.errorCode shouldBe ErrorCode.DEDUCT_INVALID_AMOUNT
    }
})
