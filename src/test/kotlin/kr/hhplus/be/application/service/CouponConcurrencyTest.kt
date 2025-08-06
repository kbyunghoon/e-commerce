package kr.hhplus.be.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.application.coupon.CouponIssueCommand
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.domain.coupon.CouponRepository
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.user.UserCouponRepository
import kr.hhplus.be.support.concurrent.ConcurrentTestExecutor
import kr.hhplus.be.support.concurrent.ConcurrentTestResult
import org.springframework.test.context.TestConstructor
import java.time.LocalDateTime

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class CouponConcurrencyTest(
    private val couponService: CouponService,
    private val couponRepository: CouponRepository,
    private val userCouponRepository: UserCouponRepository
) : DescribeSpec({

    val executor = ConcurrentTestExecutor()

    describe("쿠폰 발급 동시성 테스트") {

        it("동시에 100명이 쿠폰 발급을 요청할 때, 쿠폰 수량만큼만 발급되어야 한다") {
            // given
            val couponQuantity = 10
            val threadCount = 50
            val taskCount = 100
            val coupon = Coupon.create(
                name = "동시성 테스트 쿠폰",
                code = "CONCURRENCY_TEST_${System.currentTimeMillis()}",
                discountType = DiscountType.FIXED,
                discountValue = 1000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            // when
            val result: ConcurrentTestResult = executor.executeWithIndex(threadCount, taskCount) { taskIndex ->
                val command = CouponIssueCommand(
                    userId = taskIndex.toLong() + 1,
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            // then
            println("=== 쿠폰 발급 동시성 테스트 (100명 -> 10개 쿠폰) ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            val exceptionTypes = result.getExceptions().groupBy { it.javaClass.simpleName }
            exceptionTypes.forEach { (type, exceptions) ->
                println("$type: ${exceptions.size}회 발생")
            }

            val businessExceptions = result.getExceptions().filterIsInstance<BusinessException>()
            val errorCodeGroups = businessExceptions.groupBy { it.errorCode }
            errorCodeGroups.forEach { (errorCode, exceptions) ->
                println("$errorCode: ${exceptions.size}회 발생")
            }

            result.getSuccessCount().get() shouldBe couponQuantity
            result.getFailureCount().get() shouldBe taskCount - couponQuantity

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            println("최종 발급 수량: ${updatedCoupon.issuedQuantity}")
            println("남은 수량: ${updatedCoupon.getRemainingQuantity()}")

            updatedCoupon.issuedQuantity shouldBe couponQuantity
            updatedCoupon.getRemainingQuantity() shouldBe 0

            val issuedUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            issuedUserCoupons.size shouldBe couponQuantity
        }

        it("같은 사용자가 동시에 같은 쿠폰을 여러 번 발급 요청할 때, 한 번만 발급되어야 한다") {
            // given
            val userId = 99L
            val threadCount = 10
            val taskCount = 20
            val coupon = Coupon.create(
                name = "중복 발급 테스트 쿠폰",
                code = "DUPLICATE_TEST_${System.currentTimeMillis()}",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = 100
            )
            val savedCoupon = couponRepository.save(coupon)

            // when
            val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
                val command = CouponIssueCommand(
                    userId = userId,
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            // then
            println("=== 중복 발급 방지 테스트 (같은 사용자 20회 요청) ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            val businessExceptions = result.getExceptions().filterIsInstance<BusinessException>()
            val errorCodeGroups = businessExceptions.groupBy { it.errorCode }
            errorCodeGroups.forEach { (errorCode, exceptions) ->
                println("$errorCode: ${exceptions.size}회 발생")
            }

            result.getSuccessCount().get() shouldBe 1
            result.getFailureCount().get() shouldBe taskCount - 1

            val userCoupons = userCouponRepository.findByUserIdAndCouponId(userId, savedCoupon.id)
            userCoupons?.userId shouldBe userId

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            println("최종 발급 수량: ${updatedCoupon.issuedQuantity}")
            updatedCoupon.issuedQuantity shouldBe 1
        }

        it("쿠폰 수량이 1개일 때 100명이 동시에 발급 요청하면, 1명만 성공해야 한다") {
            // given
            val couponQuantity = 1
            val threadCount = 50
            val taskCount = 100
            val coupon = Coupon.create(
                name = "단일 쿠폰 테스트",
                code = "SINGLE_COUPON_TEST_${System.currentTimeMillis()}",
                discountType = DiscountType.FIXED,
                discountValue = 5000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            // when
            val result: ConcurrentTestResult = executor.executeWithIndex(threadCount, taskCount) { taskIndex ->
                val command = CouponIssueCommand(
                    userId = taskIndex.toLong() + 1,
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            // then
            println("=== 단일 쿠폰 경합 테스트 (100명 -> 1개 쿠폰) ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            val businessExceptions = result.getExceptions().filterIsInstance<BusinessException>()
            val errorCodeGroups = businessExceptions.groupBy { it.errorCode }
            errorCodeGroups.forEach { (errorCode, exceptions) ->
                println("$errorCode: ${exceptions.size}회 발생")
            }

            result.getSuccessCount().get() shouldBe 1
            result.getFailureCount().get() shouldBe taskCount - 1

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            println("최종 발급 수량: ${updatedCoupon.issuedQuantity}")
            println("품절 상태: ${updatedCoupon.isSoldOut()}")

            updatedCoupon.issuedQuantity shouldBe 1
            updatedCoupon.isSoldOut() shouldBe true

            val issuedUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            issuedUserCoupons.size shouldBe 1
        }

        it("쿠폰 사용과 복원이 동시에 발생할 때 데이터 일관성이 유지되어야 한다") {
            // given
            val userId = 888L
            val coupon = Coupon.create(
                name = "사용/복원 테스트 쿠폰",
                code = "USE_RESTORE_TEST_${System.currentTimeMillis()}",
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

            val threadCount = 10
            val taskCount = 20

            // when
            val useResult: ConcurrentTestResult = executor.execute(threadCount, taskCount / 2) {
                couponService.use(userId, savedCoupon.id)
            }

            val restoreResult: ConcurrentTestResult = executor.execute(threadCount, taskCount / 2) {
                couponService.restore(userId, savedCoupon.id)
            }

            // then
            println("=== 쿠폰 사용/복원 동시성 테스트 ===")
            println("사용 성공: ${useResult.getSuccessCount().get()}, 실패: ${useResult.getFailureCount().get()}")
            println("복원 성공: ${restoreResult.getSuccessCount().get()}, 실패: ${restoreResult.getFailureCount().get()}")

            val useBusinessExceptions = useResult.getExceptions().filterIsInstance<BusinessException>()
            val restoreBusinessExceptions = restoreResult.getExceptions().filterIsInstance<BusinessException>()

            if (useBusinessExceptions.isNotEmpty()) {
                val useErrorCodes = useBusinessExceptions.groupBy { it.errorCode }
                useErrorCodes.forEach { (errorCode, exceptions) ->
                    println("사용 실패 - $errorCode: ${exceptions.size}회")
                }
            }

            if (restoreBusinessExceptions.isNotEmpty()) {
                val restoreErrorCodes = restoreBusinessExceptions.groupBy { it.errorCode }
                restoreErrorCodes.forEach { (errorCode, exceptions) ->
                    println("복원 실패 - $errorCode: ${exceptions.size}회")
                }
            }

            val totalUseOperations = useResult.getSuccessCount().get() + useResult.getFailureCount().get()
            val totalRestoreOperations = restoreResult.getSuccessCount().get() + restoreResult.getFailureCount().get()

            totalUseOperations shouldBe taskCount / 2
            totalRestoreOperations shouldBe taskCount / 2

            val finalUserCoupon = userCouponRepository.findByUserIdAndCouponId(userId, savedCoupon.id)
            finalUserCoupon?.userId shouldBe userId
            println("최종 사용자 쿠폰 상태: ${finalUserCoupon?.status}")
        }
    }

    describe("쿠폰 수량 업데이트 동시성 테스트") {

        it("쿠폰 발급과 복원이 동시에 발생할 때 수량이 정확히 관리되어야 한다") {
            // given
            val couponQuantity = 5
            val threadCount = 10
            val issueTaskCount = 15
            val coupon = Coupon.create(
                name = "발급/복원 동시성 테스트",
                code = "ISSUE_RESTORE_CONCURRENCY_${System.currentTimeMillis()}",
                discountType = DiscountType.FIXED,
                discountValue = 3000,
                expiresAt = LocalDateTime.now().plusDays(1),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            val preIssuedCount = 3
            val preIssuedUserIds = mutableListOf<Long>()
            repeat(preIssuedCount) { i ->
                val preUserId = (1000 + i).toLong()
                preIssuedUserIds.add(preUserId)
                val command = CouponIssueCommand(
                    userId = preUserId,
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            // when
            val issueResult: ConcurrentTestResult = executor.executeWithIndex(threadCount, issueTaskCount) { taskIndex ->
                val command = CouponIssueCommand(
                    userId = (2000 + taskIndex).toLong(),
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            val restoreResult: ConcurrentTestResult = executor.executeWithIndex(threadCount, preIssuedCount) { taskIndex ->
                couponService.restore(preIssuedUserIds[taskIndex], savedCoupon.id)
            }

            // then
            println("=== 쿠폰 발급/복원 동시성 테스트 ===")
            println("신규 발급 성공: ${issueResult.getSuccessCount().get()}, 실패: ${issueResult.getFailureCount().get()}")
            println("복원 성공: ${restoreResult.getSuccessCount().get()}, 실패: ${restoreResult.getFailureCount().get()}")

            val issueBusinessExceptions = issueResult.getExceptions().filterIsInstance<BusinessException>()
            val restoreBusinessExceptions = restoreResult.getExceptions().filterIsInstance<BusinessException>()

            if (issueBusinessExceptions.isNotEmpty()) {
                val issueErrorCodes = issueBusinessExceptions.groupBy { it.errorCode }
                issueErrorCodes.forEach { (errorCode, exceptions) ->
                    println("발급 실패 - $errorCode: ${exceptions.size}회")
                }
            }

            if (restoreBusinessExceptions.isNotEmpty()) {
                val restoreErrorCodes = restoreBusinessExceptions.groupBy { it.errorCode }
                restoreErrorCodes.forEach { (errorCode, exceptions) ->
                    println("복원 실패 - $errorCode: ${exceptions.size}회")
                }
            }

            val finalCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            println("최종 발급 수량: ${finalCoupon.issuedQuantity}")
            println("최종 남은 수량: ${finalCoupon.getRemainingQuantity()}")

            finalCoupon.issuedQuantity shouldBeLessThanOrEqualTo finalCoupon.totalQuantity

            val totalUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            println("실제 발급된 사용자 쿠폰 수: ${totalUserCoupons.size}")
            totalUserCoupons.size shouldBe finalCoupon.issuedQuantity
        }

        it("대량 쿠폰 발급 동시성 테스트") {
            // given
            val couponQuantity = 50
            val threadCount = 30
            val taskCount = 200
            val coupon = Coupon.create(
                name = "대량 발급 테스트 쿠폰",
                code = "BULK_ISSUE_TEST_${System.currentTimeMillis()}",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 15,
                expiresAt = LocalDateTime.now().plusDays(7),
                totalQuantity = couponQuantity
            )
            val savedCoupon = couponRepository.save(coupon)

            // when
            val result: ConcurrentTestResult = executor.executeWithIndex(threadCount, taskCount) { taskIndex ->
                val command = CouponIssueCommand(
                    userId = (3000 + taskIndex).toLong(),
                    couponId = savedCoupon.id
                )
                couponService.issue(command)
            }

            // then
            println("=== 대량 쿠폰 발급 동시성 테스트 (200명 -> 50개 쿠폰) ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")
            println("성공률: ${(result.getSuccessCount().get().toDouble() / taskCount * 100).toInt()}%")

            val businessExceptions = result.getExceptions().filterIsInstance<BusinessException>()
            val errorCodeGroups = businessExceptions.groupBy { it.errorCode }
            errorCodeGroups.forEach { (errorCode, exceptions) ->
                println("$errorCode: ${exceptions.size}회 발생")
            }

            result.getSuccessCount().get() shouldBe couponQuantity
            result.getFailureCount().get() shouldBe taskCount - couponQuantity

            val updatedCoupon = couponRepository.findByIdOrThrow(savedCoupon.id)
            println("최종 발급 수량: ${updatedCoupon.issuedQuantity}")
            println("품절 여부: ${updatedCoupon.isSoldOut()}")

            updatedCoupon.issuedQuantity shouldBe couponQuantity
            updatedCoupon.isSoldOut() shouldBe true

            val issuedUserCoupons = userCouponRepository.findByCouponId(savedCoupon.id)
            issuedUserCoupons.size shouldBe couponQuantity
        }
    }
})
