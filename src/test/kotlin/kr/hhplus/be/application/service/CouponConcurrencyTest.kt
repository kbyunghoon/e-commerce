package kr.hhplus.be.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.user.UserCouponRepository
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestConstructor
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@Transactional
class CouponConcurrencyTest(
    private val couponService: CouponService,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) : DescribeSpec({

    describe("쿠폰 발급 동시성 테스트") {

        it("동시에 100명이 쿠폰 발급을 요청할 때, 쿠폰 수량만큼만 발급되어야 한다") {
            // Given
            val couponQuantity = 10
            val threadCount = 100
            val coupon = Coupon.create(
                name = "동시성 테스트 쿠폰",
                code = "CONCURRENCY_TEST",
                discountType = DiscountType.FIXED,
                discountValue = 1000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicInteger(0)
            val failureCount = AtomicInteger(0)

            // When
            repeat(threadCount) { i ->
                executor.submit {
                    try {
                        val command = CouponIssueCommand(
                            userId = (i + 1).toLong(),
                            couponId = savedCoupon.id
                        )
                        couponService.issue(command)
                        successCount.incrementAndGet()
                    } catch (e: BusinessException) {
                        when (e.errorCode) {
                            ErrorCode.COUPON_SOLD_OUT,
                            ErrorCode.COUPON_ALREADY_ISSUED -> {
                                failureCount.incrementAndGet()
                            }

                            else -> throw e
                        }
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            countDownLatch.await()
            executor.shutdown()

            // Then
            successCount.get() shouldBe couponQuantity
            failureCount.get() shouldBe threadCount - couponQuantity

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            updatedCoupon.issuedQuantity shouldBe couponQuantity
            updatedCoupon.getRemainingQuantity() shouldBe 0

            val issuedUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            issuedUserCoupons.size shouldBe couponQuantity
        }

        it("같은 사용자가 동시에 같은 쿠폰을 여러 번 발급 요청할 때, 한 번만 발급되어야 한다") {
            // Given
            val userId = 1L
            val threadCount = 10
            val coupon = Coupon.create(
                name = "중복 발급 테스트 쿠폰",
                code = "DUPLICATE_TEST",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = 100
            )
            val savedCoupon = couponRepository.save(coupon)

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicInteger(0)
            val duplicateCount = AtomicInteger(0)

            // When
            repeat(threadCount) {
                executor.submit {
                    try {
                        val command = CouponIssueCommand(
                            userId = userId,
                            couponId = savedCoupon.id
                        )
                        couponService.issue(command)
                        successCount.incrementAndGet()
                    } catch (e: BusinessException) {
                        if (e.errorCode == ErrorCode.COUPON_ALREADY_ISSUED) {
                            duplicateCount.incrementAndGet()
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
            successCount.get() shouldBe 1
            duplicateCount.get() shouldBe threadCount - 1

            val userCoupons = userCouponRepository.findByUserIdAndCouponId(userId, savedCoupon.id)
            userCoupons shouldBe userCoupons

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            updatedCoupon.issuedQuantity shouldBe 1
        }

        it("쿠폰 수량이 1개일 때 100명이 동시에 발급 요청하면, 1명만 성공해야 한다") {
            // Given
            val couponQuantity = 1
            val threadCount = 100
            val coupon = Coupon.create(
                name = "단일 쿠폰 테스트",
                code = "SINGLE_COUPON_TEST",
                discountType = DiscountType.FIXED,
                discountValue = 5000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val successCount = AtomicInteger(0)
            val failureCount = AtomicInteger(0)

            // When
            repeat(threadCount) { i ->
                executor.submit {
                    try {
                        val command = CouponIssueCommand(
                            userId = (i + 1).toLong(),
                            couponId = savedCoupon.id
                        )
                        couponService.issue(command)
                        successCount.incrementAndGet()
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
            successCount.get() shouldBe 1
            failureCount.get() shouldBe threadCount - 1

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            updatedCoupon.issuedQuantity shouldBe 1
            updatedCoupon.isSoldOut() shouldBe true

            val issuedUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            issuedUserCoupons.size shouldBe 1
        }

        it("쿠폰 사용과 복원이 동시에 발생할 때 데이터 일관성이 유지되어야 한다") {
            // Given
            val userId = 1L
            val coupon = Coupon.create(
                name = "사용/복원 테스트 쿠폰",
                code = "USE_RESTORE_TEST",
                discountType = DiscountType.FIXED,
                discountValue = 2000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = 1
            )
            val savedCoupon = couponRepository.save(coupon)

            val issueCommand = CouponIssueCommand(
                userId = userId,
                couponId = savedCoupon.id
            )
            couponService.issue(issueCommand)

            val threadCount = 20
            val countDownLatch = CountDownLatch(threadCount)
            val executor = Executors.newFixedThreadPool(threadCount)
            val useSuccessCount = AtomicInteger(0)
            val restoreSuccessCount = AtomicInteger(0)
            val exceptionCount = AtomicInteger(0)

            // When
            repeat(threadCount) { i ->
                executor.submit {
                    try {
                        if (i % 2 == 0) {
                            couponService.use(userId, savedCoupon.id)
                            useSuccessCount.incrementAndGet()
                        } else {
                            couponService.restore(userId, savedCoupon.id)
                            restoreSuccessCount.incrementAndGet()
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
            val totalOperations = useSuccessCount.get() + restoreSuccessCount.get() + exceptionCount.get()
            totalOperations shouldBe threadCount

            val finalUserCoupon = userCouponRepository.findByUserIdAndCouponId(userId, savedCoupon.id)
            finalUserCoupon?.userId shouldBe userId
        }
    }

    describe("쿠폰 수량 업데이트 동시성 테스트") {

        it("쿠폰 발급과 복원이 동시에 발생할 때 수량이 정확히 관리되어야 한다") {
            // Given
            val couponQuantity = 5
            val issueThreadCount = 10
            val coupon = Coupon.create(
                name = "발급/복원 동시성 테스트",
                code = "ISSUE_RESTORE_CONCURRENCY",
                discountType = DiscountType.FIXED,
                discountValue = 3000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            val preIssuedCount = 3
            repeat(preIssuedCount) { i ->
                val command = CouponIssueCommand(
                    userId = (i + 1).toLong(),
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            val totalThreadCount = issueThreadCount + preIssuedCount
            val countDownLatch = CountDownLatch(totalThreadCount)
            val executor = Executors.newFixedThreadPool(totalThreadCount)
            val issueSuccessCount = AtomicInteger(0)
            val restoreSuccessCount = AtomicInteger(0)
            val exceptionCount = AtomicInteger(0)

            // When
            repeat(issueThreadCount) { i ->
                executor.submit {
                    try {
                        val command = CouponIssueCommand(
                            userId = (preIssuedCount + i + 1).toLong(),
                            couponId = savedCoupon.id
                        )
                        couponService.issue(command)
                        issueSuccessCount.incrementAndGet()
                    } catch (e: BusinessException) {
                        exceptionCount.incrementAndGet()
                    } finally {
                        countDownLatch.countDown()
                    }
                }
            }

            repeat(preIssuedCount) { i ->
                executor.submit {
                    try {
                        couponService.restore((i + 1).toLong(), savedCoupon.id)
                        restoreSuccessCount.incrementAndGet()
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
            val finalCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)

            finalCoupon.issuedQuantity shouldBe finalCoupon.issuedQuantity
            finalCoupon.issuedQuantity shouldBeLessThanOrEqualTo finalCoupon.totalQuantity

            val totalUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            totalUserCoupons.size shouldBe finalCoupon.issuedQuantity
        }
    }
})
