package kr.hhplus.be.domain.user

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

class UserCouponTest : FunSpec({

    context("UserCoupon 생성 테스트") {

        test("모든 필수 정보를 제공하여 UserCoupon을 생성할 수 있다") {
            // Given
            val userId = 1L
            val couponId = 10L
            val status = CouponStatus.AVAILABLE
            val issuedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)

            // When
            val userCoupon = UserCoupon(
                userId = userId,
                couponId = couponId,
                status = status,
                issuedAt = issuedAt
            )

            // Then
            userCoupon.userId shouldBe userId
            userCoupon.couponId shouldBe couponId
            userCoupon.status shouldBe status
            userCoupon.issuedAt shouldBe issuedAt
            userCoupon.usedAt shouldBe null
        }

        test("id를 지정하지 않으면 기본값 0으로 설정된다") {
            // Given & When
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // Then
            userCoupon.id shouldBe 0
        }

        test("issuedAt을 지정하지 않으면 현재 시간으로 자동 설정된다") {
            // Given
            val beforeCreation = LocalDateTime.now().minusSeconds(1)

            // When
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            val afterCreation = LocalDateTime.now().plusSeconds(1)

            // Then
            userCoupon.issuedAt shouldNotBe null
            userCoupon.issuedAt.isAfter(beforeCreation) shouldBe true
            userCoupon.issuedAt.isBefore(afterCreation) shouldBe true
        }

        test("사용 가능한 상태로 UserCoupon을 생성할 수 있다") {
            // Given & When
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // Then
            userCoupon.status shouldBe CouponStatus.AVAILABLE
            userCoupon.isAvailable() shouldBe true
            userCoupon.isUsed() shouldBe false
            userCoupon.isExpired() shouldBe false
        }

        test("사용된 상태로 UserCoupon을 생성할 수 있다") {
            // Given & When
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.USED,
                usedAt = LocalDateTime.now()
            )

            // Then
            userCoupon.status shouldBe CouponStatus.USED
            userCoupon.isAvailable() shouldBe false
            userCoupon.isUsed() shouldBe true
            userCoupon.isExpired() shouldBe false
        }

        test("만료된 상태로 UserCoupon을 생성할 수 있다") {
            // Given & When
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.EXPIRED
            )

            // Then
            userCoupon.status shouldBe CouponStatus.EXPIRED
            userCoupon.isAvailable() shouldBe false
            userCoupon.isUsed() shouldBe false
            userCoupon.isExpired() shouldBe true
        }
    }

    context("UserCoupon 사용(use) 기능 테스트") {

        test("사용 가능한 쿠폰을 사용할 수 있다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )
            val beforeUse = LocalDateTime.now().minusSeconds(1)

            // When
            userCoupon.use()
            val afterUse = LocalDateTime.now().plusSeconds(1)

            // Then
            userCoupon.status shouldBe CouponStatus.USED
            userCoupon.isUsed() shouldBe true
            userCoupon.isAvailable() shouldBe false
            userCoupon.usedAt shouldNotBe null
            userCoupon.usedAt!!.isAfter(beforeUse) shouldBe true
            userCoupon.usedAt!!.isBefore(afterUse) shouldBe true
        }

        test("이미 사용된 쿠폰을 다시 사용하려고 하면 예외가 발생한다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.USED,
                usedAt = LocalDateTime.now()
            )

            // When & Then
            val exception = shouldThrow<BusinessException> {
                userCoupon.use()
            }
            exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
        }

        test("만료된 쿠폰을 사용하려고 하면 예외가 발생한다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.EXPIRED
            )

            // When & Then
            val exception = shouldThrow<BusinessException> {
                userCoupon.use()
            }
            exception.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
        }
    }

    context("UserCoupon 복원(restore) 기능 테스트") {

        test("사용된 쿠폰을 복원할 수 있다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.USED,
                usedAt = LocalDateTime.now()
            )

            // When
            userCoupon.restore()

            // Then
            userCoupon.status shouldBe CouponStatus.AVAILABLE
            userCoupon.isAvailable() shouldBe true
            userCoupon.isUsed() shouldBe false
            userCoupon.usedAt shouldBe null
        }

        test("사용되지 않은 쿠폰을 복원하려고 하면 예외가 발생한다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // When & Then
            val exception = shouldThrow<BusinessException> {
                userCoupon.restore()
            }
            exception.errorCode shouldBe ErrorCode.COUPON_NOT_USED
        }

        test("만료된 쿠폰을 복원하려고 하면 예외가 발생한다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.EXPIRED
            )

            // When & Then
            val exception = shouldThrow<BusinessException> {
                userCoupon.restore()
            }
            exception.errorCode shouldBe ErrorCode.COUPON_NOT_USED
        }
    }

    context("UserCoupon 만료(expire) 기능 테스트") {

        test("사용 가능한 쿠폰을 만료시킬 수 있다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // When
            userCoupon.expire()

            // Then
            userCoupon.status shouldBe CouponStatus.EXPIRED
            userCoupon.isExpired() shouldBe true
            userCoupon.isAvailable() shouldBe false
            userCoupon.isUsed() shouldBe false
        }

        test("사용된 쿠폰을 만료시킬 수 있다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.USED,
                usedAt = LocalDateTime.now()
            )

            // When
            userCoupon.expire()

            // Then
            userCoupon.status shouldBe CouponStatus.EXPIRED
            userCoupon.isExpired() shouldBe true
            userCoupon.isAvailable() shouldBe false
            userCoupon.isUsed() shouldBe false
        }

        test("이미 만료된 쿠폰을 다시 만료시킬 수 있다") {
            // Given
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.EXPIRED
            )

            // When
            userCoupon.expire()

            // Then
            userCoupon.status shouldBe CouponStatus.EXPIRED
            userCoupon.isExpired() shouldBe true
        }
    }

    context("UserCoupon 상태 확인 메서드 테스트") {

        test("isAvailable() 메서드가 올바르게 동작한다") {
            // Given
            val availableCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.AVAILABLE)
            val usedCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.USED)
            val expiredCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.EXPIRED)

            // When & Then
            availableCoupon.isAvailable() shouldBe true
            usedCoupon.isAvailable() shouldBe false
            expiredCoupon.isAvailable() shouldBe false
        }

        test("isUsed() 메서드가 올바르게 동작한다") {
            // Given
            val availableCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.AVAILABLE)
            val usedCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.USED)
            val expiredCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.EXPIRED)

            // When & Then
            availableCoupon.isUsed() shouldBe false
            usedCoupon.isUsed() shouldBe true
            expiredCoupon.isUsed() shouldBe false
        }

        test("isExpired() 메서드가 올바르게 동작한다") {
            // Given
            val availableCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.AVAILABLE)
            val usedCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.USED)
            val expiredCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.EXPIRED)

            // When & Then
            availableCoupon.isExpired() shouldBe false
            usedCoupon.isExpired() shouldBe false
            expiredCoupon.isExpired() shouldBe true
        }
    }

    context("UserCoupon 불변성 테스트") {

        test("UserCoupon 객체는 데이터 클래스로서 동등성을 지원한다") {
            // Given
            val issuedAt = LocalDateTime.now()

            val userCoupon1 = UserCoupon(
                id = 1L,
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = issuedAt
            )

            val userCoupon2 = UserCoupon(
                id = 1L,
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = issuedAt
            )

            // When & Then
            userCoupon1 shouldBe userCoupon2
            userCoupon1.hashCode() shouldBe userCoupon2.hashCode()
        }

        test("서로 다른 UserCoupon 객체는 동등하지 않다") {
            // Given
            val issuedAt = LocalDateTime.now()

            val userCoupon1 = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = issuedAt
            )

            val userCoupon2 = UserCoupon(
                userId = 2L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = issuedAt
            )

            // When & Then
            (userCoupon1 == userCoupon2) shouldBe false
        }
    }

    context("UserCoupon copy 테스트") {

        test("UserCoupon 객체를 copy하여 일부 필드를 변경할 수 있다") {
            // Given
            val originalCoupon = UserCoupon(
                id = 1L,
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = LocalDateTime.now()
            )

            // When
            val modifiedCoupon = originalCoupon.copy(userId = 2L)

            // Then
            modifiedCoupon.id shouldBe originalCoupon.id
            modifiedCoupon.userId shouldBe 2L
            modifiedCoupon.couponId shouldBe originalCoupon.couponId
            modifiedCoupon.status shouldBe originalCoupon.status
            modifiedCoupon.issuedAt shouldBe originalCoupon.issuedAt
            modifiedCoupon.usedAt shouldBe originalCoupon.usedAt
        }

        test("UserCoupon 객체를 copy하여 상태를 변경할 수 있다") {
            // Given
            val originalCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = LocalDateTime.now()
            )

            val usedAt = LocalDateTime.now()

            // When
            val modifiedCoupon = originalCoupon.copy(
                status = CouponStatus.USED,
                usedAt = usedAt
            )

            // Then
            modifiedCoupon.userId shouldBe originalCoupon.userId
            modifiedCoupon.couponId shouldBe originalCoupon.couponId
            modifiedCoupon.status shouldBe CouponStatus.USED
            modifiedCoupon.issuedAt shouldBe originalCoupon.issuedAt
            modifiedCoupon.usedAt shouldBe usedAt
        }
    }

    context("UserCoupon toString 테스트") {

        test("UserCoupon 객체의 문자열 표현을 확인할 수 있다") {
            // Given
            val userCoupon = UserCoupon(
                id = 1L,
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE,
                issuedAt = LocalDateTime.of(2024, 1, 1, 10, 0, 0)
            )

            // When
            val couponString = userCoupon.toString()

            // Then
            couponString.contains("id=1") shouldBe true
            couponString.contains("userId=1") shouldBe true
            couponString.contains("couponId=10") shouldBe true
            couponString.contains("status=AVAILABLE") shouldBe true
        }
    }

    context("CouponStatus 열거형 테스트") {

        test("모든 CouponStatus 값들이 정의되어 있다") {
            // When & Then
            CouponStatus.AVAILABLE shouldBe CouponStatus.AVAILABLE
            CouponStatus.USED shouldBe CouponStatus.USED
            CouponStatus.EXPIRED shouldBe CouponStatus.EXPIRED
        }

        test("CouponStatus는 올바른 문자열 값을 가진다") {
            // When & Then
            CouponStatus.AVAILABLE.value shouldBe "사용가능"
            CouponStatus.USED.value shouldBe "사용됨"
            CouponStatus.EXPIRED.value shouldBe "만료됨"
        }

        test("CouponStatus 값들은 서로 다르다") {
            // When & Then
            (CouponStatus.AVAILABLE == CouponStatus.USED) shouldBe false
            (CouponStatus.AVAILABLE == CouponStatus.EXPIRED) shouldBe false
            (CouponStatus.USED == CouponStatus.EXPIRED) shouldBe false
        }

        test("CouponStatus.values()로 모든 값을 조회할 수 있다") {
            // When
            val values = CouponStatus.values()

            // Then
            values.size shouldBe 3
            values.contains(CouponStatus.AVAILABLE) shouldBe true
            values.contains(CouponStatus.USED) shouldBe true
            values.contains(CouponStatus.EXPIRED) shouldBe true
        }

        test("CouponStatus.valueOf()로 문자열에서 열거형을 생성할 수 있다") {
            // When & Then
            CouponStatus.valueOf("AVAILABLE") shouldBe CouponStatus.AVAILABLE
            CouponStatus.valueOf("USED") shouldBe CouponStatus.USED
            CouponStatus.valueOf("EXPIRED") shouldBe CouponStatus.EXPIRED
        }
    }

    context("UserCoupon 실제 사용 시나리오 테스트") {

        test("쿠폰 발급부터 사용까지의 전체 플로우") {
            // Given - 쿠폰 발급
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // When & Then - 초기 상태 확인
            userCoupon.isAvailable() shouldBe true
            userCoupon.isUsed() shouldBe false
            userCoupon.isExpired() shouldBe false
            userCoupon.usedAt shouldBe null

            // When - 쿠폰 사용
            userCoupon.use()

            // Then - 사용 후 상태 확인
            userCoupon.isAvailable() shouldBe false
            userCoupon.isUsed() shouldBe true
            userCoupon.isExpired() shouldBe false
            userCoupon.usedAt shouldNotBe null
        }

        test("쿠폰 사용 후 복원하는 시나리오") {
            // Given - 사용된 쿠폰
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )
            userCoupon.use()

            // When - 쿠폰 복원
            userCoupon.restore()

            // Then - 복원 후 상태 확인
            userCoupon.isAvailable() shouldBe true
            userCoupon.isUsed() shouldBe false
            userCoupon.isExpired() shouldBe false
            userCoupon.usedAt shouldBe null
        }

        test("쿠폰 만료 시나리오") {
            // Given - 사용 가능한 쿠폰
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // When - 쿠폰 만료
            userCoupon.expire()

            // Then - 만료 후 상태 확인
            userCoupon.isAvailable() shouldBe false
            userCoupon.isUsed() shouldBe false
            userCoupon.isExpired() shouldBe true

            // When & Then - 만료된 쿠폰은 사용할 수 없음
            shouldThrow<BusinessException> {
                userCoupon.use()
            }.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE
        }

        test("복수 사용자의 동일 쿠폰 발급 시나리오") {
            // Given & When
            val userCoupon1 = UserCoupon(
                userId = 1L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            val userCoupon2 = UserCoupon(
                userId = 2L,
                couponId = 10L,
                status = CouponStatus.AVAILABLE
            )

            // Then - 동일한 쿠폰이지만 서로 다른 사용자 쿠폰
            userCoupon1.couponId shouldBe userCoupon2.couponId
            userCoupon1.userId shouldNotBe userCoupon2.userId
            (userCoupon1 == userCoupon2) shouldBe false

            // When - 한 사용자만 쿠폰 사용
            userCoupon1.use()

            // Then - 다른 사용자의 쿠폰은 영향 받지 않음
            userCoupon1.isUsed() shouldBe true
            userCoupon2.isAvailable() shouldBe true
        }

        test("쿠폰 상태 변경 불가 시나리오들") {
            // Given
            val availableCoupon = UserCoupon(1L, 10L, 1L, CouponStatus.AVAILABLE)
            val usedCoupon = UserCoupon(1L, 11L, 1L, CouponStatus.USED, usedAt = LocalDateTime.now())
            val expiredCoupon = UserCoupon(1L, 12L, 1L, CouponStatus.EXPIRED)

            // When & Then - 사용할 수 없는 쿠폰들
            shouldThrow<BusinessException> {
                usedCoupon.use()
            }.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE

            shouldThrow<BusinessException> {
                expiredCoupon.use()
            }.errorCode shouldBe ErrorCode.COUPON_NOT_AVAILABLE

            // When & Then - 복원할 수 없는 쿠폰들
            shouldThrow<BusinessException> {
                availableCoupon.restore()
            }.errorCode shouldBe ErrorCode.COUPON_NOT_USED

            shouldThrow<BusinessException> {
                expiredCoupon.restore()
            }.errorCode shouldBe ErrorCode.COUPON_NOT_USED
        }
    }

    context("UserCoupon 경계값 테스트") {

        test("최소값으로 UserCoupon을 생성할 수 있다") {
            // Given & When
            val userCoupon = UserCoupon(
                userId = 1L,
                couponId = 1L,
                status = CouponStatus.AVAILABLE
            )

            // Then
            userCoupon.userId shouldBe 1L
            userCoupon.couponId shouldBe 1L
        }

        test("큰 ID 값으로 UserCoupon을 생성할 수 있다") {
            // Given & When
            val userCoupon = UserCoupon(
                userId = Long.MAX_VALUE,
                couponId = Long.MAX_VALUE,
                status = CouponStatus.AVAILABLE
            )

            // Then
            userCoupon.userId shouldBe Long.MAX_VALUE
            userCoupon.couponId shouldBe Long.MAX_VALUE
        }
    }
})
