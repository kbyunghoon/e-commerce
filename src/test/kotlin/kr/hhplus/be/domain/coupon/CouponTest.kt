package kr.hhplus.be.domain.coupon

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime

class CouponTest : FunSpec({

    test("발급 가능한 수량이 있을 때 발급하면 발급 수량이 증가한다") {
        // Given
        val coupon = Coupon(
            id = 1L,
            name = "신규가입 쿠폰",
            code = "WELCOME2024",
            discountType = DiscountType.FIXED,
            discountValue = 5000,
            expiresAt = LocalDateTime.now().plusDays(30),
            totalQuantity = 100,
            issuedQuantity = 50
        )

        // When
        coupon.issue()

        // Then
        coupon.issuedQuantity shouldBe 51
    }

    test("발급 수량이 모두 소진된 상태에서 발급하면 예외가 발생한다") {
        // Given
        val soldOutCoupon = Coupon(
            id = 2L,
            name = "품절 쿠폰",
            code = "SOLDOUT",
            discountType = DiscountType.FIXED,
            discountValue = 1000,
            expiresAt = LocalDateTime.now().plusDays(7),
            totalQuantity = 10,
            issuedQuantity = 10
        )

        // When & Then
        shouldThrow<BusinessException> {
            soldOutCoupon.issue()
        }.errorCode shouldBe ErrorCode.COUPON_SOLD_OUT
    }

    test("정액 할인 쿠폰은 할인액만큼 차감되며, 원금보다 클 수 없다") {
        // Given
        val fixedCoupon = Coupon(
            id = 1L,
            name = "정액할인 쿠폰",
            code = "FIXED5000",
            discountType = DiscountType.FIXED,
            discountValue = 5000,
            expiresAt = LocalDateTime.now().plusDays(30),
            totalQuantity = 100,
            issuedQuantity = 0
        )

        // Then
        fixedCoupon.calculateDiscount(20000) shouldBe 5000
        fixedCoupon.calculateDiscount(3000) shouldBe 3000
    }

    test("정율 할인 쿠폰은 비율에 따라 차감되며, 원금보다 클 수 없다") {
        // Given
        val percentageCoupon = Coupon(
            id = 2L,
            name = "정율할인 쿠폰",
            code = "PERCENT10",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            expiresAt = LocalDateTime.now().plusDays(30),
            totalQuantity = 100,
            issuedQuantity = 0
        )

        // Then
        percentageCoupon.calculateDiscount(20000) shouldBe 2000
        percentageCoupon.calculateDiscount(50000) shouldBe 5000
        percentageCoupon.calculateDiscount(1000) shouldBe 100
    }
})
