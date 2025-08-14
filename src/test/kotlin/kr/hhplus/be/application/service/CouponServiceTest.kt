package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.UserCoupon
import kr.hhplus.be.domain.user.UserCouponRepository
import java.time.LocalDateTime

class CouponServiceTest : BehaviorSpec({
    val couponRepository: CouponRepository = mockk()
    val userCouponRepository: UserCouponRepository = mockk()
    val couponService = CouponService(couponRepository, userCouponRepository)

    afterContainer {
        clearAllMocks()
    }

    Given("쿠폰 발급(issue) 시나리오") {
        val userId = 1L
        val couponId = 1L
        val command = CouponIssueCommand(userId, couponId)
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(7)
        val coupon = Coupon(
            id = couponId,
            name = "테스트 쿠폰",
            code = "TEST1234",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            expiresAt = expiresAt,
            totalQuantity = 100,
            issuedQuantity = 0,
            createdAt = now,
            updatedAt = now
        )

        When("유효한 쿠폰 발급을 요청하면") {
            every { couponRepository.findByIdOrThrow(couponId) } returns coupon
            every { userCouponRepository.existsByUserIdAndCouponId(userId, couponId) } returns false
            every { couponRepository.save(any()) } answers { it.invocation.args[0] as Coupon }
            every { userCouponRepository.save(any()) } answers { it.invocation.args[0] as UserCoupon }

            val result = couponService.issue(command)

            Then("쿠폰이 성공적으로 발급되고, 발급된 쿠폰 정보가 반환된다") {
                result.userId shouldBe userId
                result.couponId shouldBe couponId
                result.status shouldBe CouponStatus.AVAILABLE
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 1) { userCouponRepository.existsByUserIdAndCouponId(userId, couponId) }
                verify(exactly = 1) { couponRepository.save(any()) }
                verify(exactly = 1) { userCouponRepository.save(any()) }
            }
        }

        When("존재하지 않는 쿠폰 ID로 발급을 요청하면") {
            every { couponRepository.findByIdOrThrow(couponId) } throws BusinessException(ErrorCode.COUPON_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                couponService.issue(command)
            }

            Then("COUPON_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_FOUND
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 0) { userCouponRepository.existsByUserIdAndCouponId(any(), any()) }
            }
        }

        When("만료된 쿠폰으로 발급을 요청하면") {
            val expiredCoupon = coupon.copy(expiresAt = now.minusDays(1))
            every { couponRepository.findByIdOrThrow(couponId) } returns expiredCoupon

            val exception = shouldThrow<BusinessException> {
                couponService.issue(command)
            }

            Then("COUPON_EXPIRED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_EXPIRED
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 0) { userCouponRepository.existsByUserIdAndCouponId(any(), any()) }
            }
        }

        When("이미 발급된 쿠폰으로 발급을 요청하면") {
            every { couponRepository.findByIdOrThrow(couponId) } returns coupon
            every { userCouponRepository.existsByUserIdAndCouponId(userId, couponId) } returns true

            val exception = shouldThrow<BusinessException> {
                couponService.issue(command)
            }

            Then("COUPON_ALREADY_ISSUED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_ALREADY_ISSUED
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 1) { userCouponRepository.existsByUserIdAndCouponId(userId, couponId) }
            }
        }

        When("재고가 소진된 쿠폰으로 발급을 요청하면") {
            val soldOutCoupon = coupon.copy(issuedQuantity = coupon.totalQuantity)
            every { couponRepository.findByIdOrThrow(couponId) } returns soldOutCoupon

            val exception = shouldThrow<BusinessException> {
                couponService.issue(command)
            }

            Then("COUPON_SOLD_OUT 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_SOLD_OUT
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 0) { userCouponRepository.existsByUserIdAndCouponId(userId, couponId) }
            }
        }
    }

    Given("쿠폰 사용(use) 시나리오") {
        val userId = 1L
        val couponId = 1L
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(7)
        val coupon = Coupon(
            id = couponId,
            name = "테스트 쿠폰",
            code = "TEST1234",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            expiresAt = expiresAt,
            totalQuantity = 100,
            issuedQuantity = 1,
            createdAt = now,
            updatedAt = now
        )
        val userCoupon = UserCoupon(
            id = 1L,
            userId = userId,
            couponId = couponId,
            status = CouponStatus.AVAILABLE,
            issuedAt = now
        )

        When("유효한 쿠폰 사용을 요청하면") {
            every { userCouponRepository.findByUserIdAndCouponId(userId, couponId) } returns userCoupon
            every { couponRepository.findByIdOrThrow(couponId) } returns coupon
            every { userCouponRepository.save(any()) } answers { it.invocation.args[0] as UserCoupon }

            val result = couponService.use(userId, couponId)

            Then("쿠폰이 성공적으로 사용되고, 사용된 쿠폰 정보가 반환된다") {
                result.userId shouldBe userId
                result.couponId shouldBe couponId
                result.status shouldBe CouponStatus.USED
                verify(exactly = 1) { userCouponRepository.findByUserIdAndCouponId(userId, couponId) }
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 1) { userCouponRepository.save(any()) }
            }
        }

        When("존재하지 않는 사용자 쿠폰으로 사용을 요청하면") {
            every { userCouponRepository.findByUserIdAndCouponId(userId, couponId) } returns null

            val exception = shouldThrow<BusinessException> {
                couponService.use(userId, couponId)
            }

            Then("USER_COUPON_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.USER_COUPON_NOT_FOUND
                verify(exactly = 1) { userCouponRepository.findByUserIdAndCouponId(userId, couponId) }
                verify(exactly = 0) { couponRepository.findByIdOrThrow(any()) }
            }
        }

        When("만료된 쿠폰으로 사용을 요청하면") {
            val expiredCoupon = coupon.copy(expiresAt = now.minusDays(1))
            every { userCouponRepository.findByUserIdAndCouponId(userId, couponId) } returns userCoupon
            every { couponRepository.findByIdOrThrow(couponId) } returns expiredCoupon
            every { userCouponRepository.save(any()) } answers { it.invocation.args[0] as UserCoupon }

            val exception = shouldThrow<BusinessException> {
                couponService.use(userId, couponId)
            }

            Then("COUPON_EXPIRED 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_EXPIRED
                verify(exactly = 1) { userCouponRepository.findByUserIdAndCouponId(userId, couponId) }
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 1) { userCouponRepository.save(any()) }
            }
        }

        When("이미 사용된 쿠폰으로 사용을 요청하면") {
            val usedUserCoupon = userCoupon.copy(status = CouponStatus.USED)
            every { userCouponRepository.findByUserIdAndCouponId(userId, couponId) } returns usedUserCoupon
            every { couponRepository.findByIdOrThrow(couponId) } returns coupon

            val exception = shouldThrow<BusinessException> {
                couponService.use(userId, couponId)
            }

            Then("COUPON_NOT_AVAILABLE 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
                verify(exactly = 1) { userCouponRepository.findByUserIdAndCouponId(userId, couponId) }
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
                verify(exactly = 0) { userCouponRepository.save(any()) }
            }
        }
    }

    Given("할인 금액 계산(calculateDiscount) 시나리오") {
        val userId = 1L
        val couponId = 1L
        val originalAmount = 10000
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(7)
        val discountValue = 10

        When("비율 할인 쿠폰으로 계산을 요청하면") {
            val percentageCoupon = Coupon(
                id = couponId,
                name = "10% 할인",
                code = "PERCENT10",
                discountType = DiscountType.PERCENTAGE,
                discountValue = discountValue,
                expiresAt = expiresAt,
                totalQuantity = 100,
                issuedQuantity = 1,
                createdAt = now,
                updatedAt = now
            )
            every { couponRepository.findByIdOrThrow(couponId) } returns percentageCoupon

            val discount = couponService.calculateDiscount(userId, couponId, originalAmount)

            Then("정확한 비율 할인 금액이 반환된다") {
                discount shouldBe originalAmount / 100 * discountValue
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
            }
        }

        When("정액 할인 쿠폰으로 계산을 요청하면") {
            val fixedCoupon = Coupon(
                id = couponId,
                name = "5000원 할인",
                code = "FIXED5000",
                discountType = DiscountType.FIXED,
                discountValue = 5000,
                expiresAt = expiresAt,
                totalQuantity = 100,
                issuedQuantity = 1,
                createdAt = now,
                updatedAt = now
            )
            every { couponRepository.findByIdOrThrow(couponId) } returns fixedCoupon

            val discount = couponService.calculateDiscount(userId, couponId, originalAmount)

            Then("정확한 정액 할인 금액이 반환된다") {
                discount shouldBe 5000
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
            }
        }

        When("할인 금액이 원금보다 큰 경우") {
            val fixedCoupon = Coupon(
                id = couponId,
                name = "15000원 할인",
                code = "FIXED15000",
                discountType = DiscountType.FIXED,
                discountValue = 15000,
                expiresAt = expiresAt,
                totalQuantity = 100,
                issuedQuantity = 1,
                createdAt = now,
                updatedAt = now
            )
            every { couponRepository.findByIdOrThrow(couponId) } returns fixedCoupon

            val discount = couponService.calculateDiscount(userId, couponId, originalAmount)

            Then("할인 금액은 원금을 초과할 수 없다") {
                discount shouldBe originalAmount
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
            }
        }

        When("존재하지 않는 쿠폰 ID로 계산을 요청하면") {
            every { couponRepository.findByIdOrThrow(couponId) } throws BusinessException(ErrorCode.COUPON_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                couponService.calculateDiscount(userId, couponId, originalAmount)
            }

            Then("COUPON_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_FOUND
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId) }
            }
        }
    }

    Given("사용자 쿠폰 조회(getUserCoupons) 시나리오") {
        val userId = 1L
        val couponId1 = 1L
        val couponId2 = 2L
        val now = LocalDateTime.now()
        val expiresAt = now.plusDays(7)

        val coupon1 = Coupon(
            id = couponId1,
            name = "쿠폰1",
            code = "CODE1",
            discountType = DiscountType.FIXED,
            discountValue = 1000,
            expiresAt = expiresAt,
            totalQuantity = 100,
            issuedQuantity = 1,
            createdAt = now,
            updatedAt = now
        )
        val coupon2 = Coupon(
            id = couponId2,
            name = "쿠폰2",
            code = "CODE2",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 5,
            expiresAt = expiresAt,
            totalQuantity = 100,
            issuedQuantity = 1,
            createdAt = now,
            updatedAt = now
        )

        val userCoupon1 = UserCoupon(
            id = 1L,
            userId = userId,
            couponId = couponId1,
            status = CouponStatus.AVAILABLE,
            issuedAt = now
        )
        val userCoupon2 = UserCoupon(
            id = 2L,
            userId = userId,
            couponId = couponId2,
            status = CouponStatus.USED,
            issuedAt = now
        )

        When("사용자 ID로 쿠폰 목록 조회를 요청하면") {
            every { userCouponRepository.findByUserId(userId) } returns listOf(userCoupon1, userCoupon2)
            every { couponRepository.findByIdOrThrow(couponId1) } returns coupon1
            every { couponRepository.findByIdOrThrow(couponId2) } returns coupon2

            val results = couponService.getUserCoupons(userId)

            Then("해당 사용자의 모든 쿠폰 정보가 반환된다") {
                results.size shouldBe 2
                results[0].couponId shouldBe couponId1
                results[0].status shouldBe CouponStatus.AVAILABLE
                results[1].couponId shouldBe couponId2
                results[1].status shouldBe CouponStatus.USED

                verify(exactly = 1) { userCouponRepository.findByUserId(userId) }
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId1) }
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId2) }
            }
        }

        When("사용자 ID로 쿠폰 목록 조회를 요청하지만, 쿠폰 정보가 없는 경우") {
            every { userCouponRepository.findByUserId(userId) } returns listOf(userCoupon1)
            every { couponRepository.findByIdOrThrow(couponId1) } throws BusinessException(ErrorCode.COUPON_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                couponService.getUserCoupons(userId)
            }

            Then("COUPON_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_FOUND
                verify(exactly = 1) { userCouponRepository.findByUserId(userId) }
                verify(exactly = 1) { couponRepository.findByIdOrThrow(couponId1) }
            }
        }
    }
})