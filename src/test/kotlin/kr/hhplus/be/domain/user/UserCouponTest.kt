package kr.hhplus.be.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

class UserCouponTest : BehaviorSpec({

    Given("UserCoupon 객체 생성 시나리오") {
        val userId = 1L
        val couponId = 10L
        val status = CouponStatus.AVAILABLE
        val issuedAt = LocalDateTime.now()

        When("모든 필수 정보를 제공하여 UserCoupon을 생성하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = status,
                issuedAt = issuedAt
            )

            Then("UserCoupon 객체가 성공적으로 생성되고, 제공된 값들을 포함한다") {
                userCoupon.userId shouldBe userId
                userCoupon.couponId shouldBe couponId
                userCoupon.status shouldBe status
                userCoupon.issuedAt shouldBe issuedAt
                userCoupon.usedAt shouldBe null
            }
        }

        When("id를 지정하지 않고 UserCoupon을 생성하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = status,
                issuedAt = issuedAt
            )

            Then("id가 기본값인 0으로 설정된다") {
                userCoupon.id shouldBe 0
            }
        }

        When("issuedAt을 지정하지 않고 UserCoupon을 생성하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = status
            )

            Then("issuedAt이 현재 시간으로 자동 설정된다") {
                userCoupon.issuedAt.withNano(0) shouldBe LocalDateTime.now().withNano(0)
            }
        }
    }

    Given("UserCoupon 사용(use) 시나리오") {
        val userId = 1L
        val couponId = 10L

        When("쿠폰 상태가 AVAILABLE일 때 use() 메소드를 호출하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = CouponStatus.AVAILABLE
            )
            userCoupon.use()

            Then("쿠폰 상태가 USED로 변경되고, usedAt이 현재 시간으로 설정된다") {
                userCoupon.status shouldBe CouponStatus.USED
                userCoupon.usedAt?.withNano(0) shouldBe LocalDateTime.now().withNano(0)
            }
        }

        When("쿠폰 상태가 USED일 때 use() 메소드를 호출하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = CouponStatus.USED
            )
            val exception = shouldThrow<BusinessException> {
                userCoupon.use()
            }

            Then("COUPON_NOT_AVAILABLE 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
            }
        }

        When("쿠폰 상태가 EXPIRED일 때 use() 메소드를 호출하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = CouponStatus.EXPIRED
            )
            val exception = shouldThrow<BusinessException> {
                userCoupon.use()
            }

            Then("COUPON_NOT_AVAILABLE 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
            }
        }
    }

    Given("UserCoupon 만료(expire) 시나리오") {
        val userId = 1L
        val couponId = 10L

        When("expire() 메소드를 호출하면") {
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = CouponStatus.AVAILABLE
            )
            userCoupon.expire()

            Then("쿠폰 상태가 EXPIRED로 변경된다") {
                userCoupon.status shouldBe CouponStatus.EXPIRED
            }
        }
    }

    Given("UserCoupon toEntity() 변환 시나리오") {
        val userId = 1L
        val couponId = 10L
        val status = CouponStatus.AVAILABLE
        val issuedAt = LocalDateTime.now()
        val usedAt = LocalDateTime.now().plusHours(1)

        val userCoupon = UserCoupon(
            id = 1L,
            userId = userId,
            couponId = couponId,
            status = status,
            issuedAt = issuedAt,
            usedAt = usedAt
        )

        When("toEntity() 메소드를 호출하면") {
            val entity = userCoupon.toEntity()

            Then("UserCouponEntity 객체가 성공적으로 생성되고, 동일한 값들을 포함한다") {
                entity.id shouldBe userCoupon.id
                entity.userId shouldBe userCoupon.userId
                entity.couponId shouldBe userCoupon.couponId
                entity.status shouldBe userCoupon.status
                entity.issuedAt shouldBe userCoupon.issuedAt
                entity.usedAt shouldBe userCoupon.usedAt
            }
        }
    }
})