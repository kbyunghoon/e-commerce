package kr.hhplus.be.domain.coupon

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

class CouponConcurrencyTest : DescribeSpec({

    describe("쿠폰 도메인 객체 동시성 테스트") {

        it("동일한 쿠폰 객체에 대한 동시 발급 호출 시 수량이 정확히 관리되어야 한다") {
            // Given
            val totalQuantity = 10
            val threadCount = 50

            val coupon = Coupon.create(
                name = "동시성 테스트 쿠폰",
                code = "CONCURRENCY_DOMAIN_TEST",
                discountType = DiscountType.FIXED,
                discountValue = 1000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = totalQuantity
            )

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicInteger(0)
            val failureCount = AtomicInteger(0)

            // When
            repeat(threadCount) {
                executor.submit {
                    try {
                        synchronized(coupon) {
                            if (coupon.canBeIssued()) {
                                coupon.issue()
                                successCount.incrementAndGet()
                            } else {
                                failureCount.incrementAndGet()
                            }
                        }
                    } catch (e: BusinessException) {
                        if (e.errorCode == ErrorCode.COUPON_SOLD_OUT) {
                            failureCount.incrementAndGet()
                        } else {
                            throw e
                        }
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            successCount.get() shouldBe totalQuantity
            failureCount.get() shouldBe threadCount - totalQuantity
            coupon.issuedQuantity shouldBe totalQuantity
            coupon.getRemainingQuantity() shouldBe 0
            coupon.isSoldOut() shouldBe true
        }

        it("쿠폰 발급과 복원이 동시에 발생할 때 수량이 음수가 되지 않아야 한다") {
            // Given
            val coupon = Coupon.create(
                name = "발급/복원 테스트",
                code = "ISSUE_RESTORE_DOMAIN_TEST",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 15,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = 5
            )

            repeat(3) {
                coupon.issue()
            }

            val threadCount = 20
            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val issueSuccessCount = AtomicInteger(0)
            val restoreSuccessCount = AtomicInteger(0)
            val exceptionCount = AtomicInteger(0)

            // When
            repeat(threadCount) { i ->
                executor.submit {
                    try {
                        synchronized(coupon) {
                            if (i % 2 == 0) {
                                if (coupon.canBeIssued()) {
                                    coupon.issue()
                                    issueSuccessCount.incrementAndGet()
                                }
                            } else {
                                if (coupon.issuedQuantity > 0) {
                                    coupon.restore()
                                    restoreSuccessCount.incrementAndGet()
                                }
                            }
                        }
                    } catch (e: BusinessException) {
                        exceptionCount.incrementAndGet()
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            coupon.issuedQuantity shouldBeGreaterThanOrEqualTo 0
            coupon.issuedQuantity shouldBeLessThanOrEqualTo coupon.totalQuantity
        }

        it("여러 쿠폰 객체에 대한 할인 계산이 동시에 수행되어도 올바른 결과를 반환해야 한다") {
            // Given
            val coupons = listOf(
                Coupon.create(
                    name = "고정 할인 쿠폰",
                    code = "FIXED_DISCOUNT",
                    discountType = DiscountType.FIXED,
                    discountValue = 1000,
                    expiresAt = LocalDateTime.now().plusDays(1),
                    totalQuantity = 100
                ),
                Coupon.create(
                    name = "퍼센트 할인 쿠폰",
                    code = "PERCENT_DISCOUNT",
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = 20,
                    expiresAt = LocalDateTime.now().plusDays(1),
                    totalQuantity = 100
                )
            )

            val threadCount = 100
            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val results = mutableListOf<Int>()
            val resultLock = Any()

            // When
            repeat(threadCount) { i ->
                executor.submit {
                    try {
                        val coupon = coupons[i % 2]
                        val originalAmount = 10000
                        val discount = coupon.calculateDiscount(originalAmount)

                        synchronized(resultLock) {
                            results.add(discount)
                        }
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            results.size shouldBe threadCount

            val fixedDiscountResults = results.filterIndexed { index, _ -> index % 2 == 0 }
            val percentDiscountResults = results.filterIndexed { index, _ -> index % 2 == 1 }

            fixedDiscountResults.all { it == 1000 } shouldBe true
            percentDiscountResults.all { it == 2000 } shouldBe true
        }

        it("쿠폰의 가용성 확인이 동시에 수행되어도 일관된 결과를 반환해야 한다") {
            // Given
            val coupon = Coupon.create(
                name = "가용성 테스트 쿠폰",
                code = "AVAILABILITY_TEST",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = 3
            )

            val threadCount = 100
            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val availabilityResults = mutableListOf<Boolean>()
            val remainingQuantityResults = mutableListOf<Int>()
            val resultLock = Any()

            // When
            repeat(threadCount) {
                executor.submit {
                    try {
                        val isAvailable = coupon.isAvailable()
                        val remainingQuantity = coupon.getRemainingQuantity()

                        synchronized(resultLock) {
                            availabilityResults.add(isAvailable)
                            remainingQuantityResults.add(remainingQuantity)
                        }
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            availabilityResults.size shouldBe threadCount
            remainingQuantityResults.size shouldBe threadCount

            availabilityResults.all { it } shouldBe true
            remainingQuantityResults.all { it == 3 } shouldBe true
        }
    }
})
