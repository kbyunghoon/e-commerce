package kr.hhplus.be.application.service

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.domain.product.ProductStatus
import kr.hhplus.be.support.concurrent.ConcurrentTestExecutor
import kr.hhplus.be.support.concurrent.ConcurrentTestResult
import org.springframework.test.context.TestConstructor
import java.time.LocalDateTime

@IntegrationTest
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ProductConcurrencyTest(
    private val productService: ProductService,
    private val productRepository: ProductRepository
) : DescribeSpec({

    val executor = ConcurrentTestExecutor()

    describe("상품 동시성 테스트") {

        it("재고 차감 동시성 테스트 - 기본") {
            // given
            val initialStock = 100
            val threadCount = 10
            val taskCount = 20

            val product = Product(
                id = 0L,
                name = "재고차감 테스트 상품",
                stock = initialStock,
                price = 1000,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = ProductStatus.ACTIVE,
                version = 0L
            )
            val savedProduct = productRepository.save(product)

            // when
            val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
                productService.deductStock(savedProduct.id, 1)
            }

            // then
            println("=== 재고 차감 동시성 테스트 - 기본 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            result.getExceptions().forEach { exception ->
                println("예외: ${exception.javaClass.simpleName} - ${exception.message}")
            }

            val finalProduct = productRepository.findByIdOrThrow(savedProduct.id)
            val finalStock = finalProduct.stock
            println("초기 재고: $initialStock")
            println("최종 재고: $finalStock")
            println("차감된 재고: ${initialStock - finalStock}")

            finalStock shouldBeGreaterThanOrEqualTo 0

            val expectedStock = initialStock - result.getSuccessCount().get()
            finalStock shouldBe expectedStock
        }

        it("재고 차감 동시성 테스트 - 높은 동시성") {
            // given
            val initialStock = 50
            val threadCount = 20
            val taskCount = 100

            val product = Product(
                id = 0L,
                name = "높은 동시성 테스트 상품",
                stock = initialStock,
                price = 2000,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = ProductStatus.ACTIVE,
                version = 0L
            )
            val savedProduct = productRepository.save(product)

            // when
            val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
                productService.deductStock(savedProduct.id, 1)
            }

            // then
            println("=== 재고 차감 동시성 테스트 - 높은 동시성 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")
            println("성공률: ${(result.getSuccessCount().get().toDouble() / taskCount * 100).toInt()}%")

            val exceptionTypes = result.getExceptions().groupBy { it.javaClass.simpleName }
            exceptionTypes.forEach { (type, exceptions) ->
                println("$type: ${exceptions.size}회 발생")
            }

            val finalProduct = productRepository.findByIdOrThrow(savedProduct.id)
            val finalStock = finalProduct.stock
            val totalDeducted = initialStock - finalStock
            println("초기 재고: $initialStock")
            println("최종 재고: $finalStock")
            println("총 차감된 재고: $totalDeducted")

            finalStock shouldBeGreaterThanOrEqualTo 0

            result.getSuccessCount().get() shouldBeLessThanOrEqualTo initialStock

            val expectedStock = initialStock - result.getSuccessCount().get()
            finalStock shouldBe expectedStock
        }

        it("재고 부족 상황 동시성 테스트") {
            // given
            val initialStock = 5
            val threadCount = 10
            val taskCount = 20

            val product = Product(
                id = 0L,
                name = "재고부족 테스트 상품",
                stock = initialStock,
                price = 3000,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = ProductStatus.ACTIVE,
                version = 0L
            )
            val savedProduct = productRepository.save(product)

            // when
            val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
                productService.deductStock(savedProduct.id, 1)
            }

            // then
            println("=== 재고 부족 상황 동시성 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")

            val exceptionTypes = result.getExceptions().groupBy { it.javaClass.simpleName }
            exceptionTypes.forEach { (type, exceptions) ->
                println("$type: ${exceptions.size}회 발생")
            }

            val finalProduct = productRepository.findByIdOrThrow(savedProduct.id)
            val finalStock = finalProduct.stock
            println("초기 재고: $initialStock")
            println("최종 재고: $finalStock")
            println("차감된 재고: ${initialStock - finalStock}")

            finalStock shouldBe 0

            result.getSuccessCount().get() shouldBe initialStock

            result.getFailureCount().get() shouldBe (taskCount - initialStock)
        }

        it("대량 재고 차감 동시성 테스트") {
            // given
            val initialStock = 1000
            val threadCount = 50
            val taskCount = 100
            val deductAmount = 5

            val product = Product(
                id = 0L,
                name = "대량 차감 테스트 상품",
                stock = initialStock,
                price = 5000,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                status = ProductStatus.ACTIVE,
                version = 0L
            )
            val savedProduct = productRepository.save(product)

            // when
            val result: ConcurrentTestResult = executor.execute(threadCount, taskCount) {
                productService.deductStock(savedProduct.id, deductAmount)
            }

            // then
            println("=== 대량 재고 차감 동시성 테스트 ===")
            println("성공 카운트: ${result.getSuccessCount().get()}")
            println("실패 카운트: ${result.getFailureCount().get()}")
            println("성공률: ${(result.getSuccessCount().get().toDouble() / taskCount * 100).toInt()}%")

            if (result.getExceptions().isNotEmpty()) {
                val exceptionTypes = result.getExceptions().groupBy { it.javaClass.simpleName }
                exceptionTypes.forEach { (type, exceptions) ->
                    println("$type: ${exceptions.size}회 발생")
                }
            }

            val finalProduct = productRepository.findByIdOrThrow(savedProduct.id)
            val finalStock = finalProduct.stock
            val totalDeducted = initialStock - finalStock
            println("초기 재고: $initialStock")
            println("최종 재고: $finalStock")
            println("총 차감된 재고: $totalDeducted")
            println("예상 차감량: ${result.getSuccessCount().get() * deductAmount}")

            finalStock shouldBeGreaterThanOrEqualTo 0

            val expectedStock = initialStock - (result.getSuccessCount().get() * deductAmount)
            finalStock shouldBe expectedStock
        }
    }
})
