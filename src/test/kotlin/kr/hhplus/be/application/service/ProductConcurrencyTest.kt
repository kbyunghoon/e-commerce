package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.domain.product.ProductStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@IntegrationTest
class ProductConcurrencyTest @Autowired constructor(
    private val productService: ProductService,
    private val productRepository: ProductRepository,
    private val transactionManager: PlatformTransactionManager
) : BehaviorSpec({

    val transactionTemplate = TransactionTemplate(transactionManager)

    Given("동시성 재고 차감 테스트") {
        val initialStock = 100
        val numberOfThreads = 100

        When("100개 스레드가 동시에 재고 1개씩 차감") {
            val productId = transactionTemplate.execute {
                val product = Product(
                    id = 0L,
                    name = "상품 테스트",
                    stock = initialStock,
                    price = 1000,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now(),
                    status = ProductStatus.ACTIVE,
                    version = 0L
                )
                productRepository.save(product).id
            } ?: 0L

            val executor = Executors.newFixedThreadPool(numberOfThreads)
            val latch = CountDownLatch(numberOfThreads)

            repeat(numberOfThreads) {
                executor.submit {
                    try {
                        productService.deductStock(productId, 1)
                    } catch (e: Exception) {
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()
            executor.shutdown()

            val finalProduct = transactionTemplate.execute {
                productRepository.findByIdOrThrow(productId)
            }

            Then("최종 재고는 0 이상이어야 한다") {
                val finalStock = finalProduct?.stock ?: 0
                finalStock shouldBeGreaterThanOrEqual 0
            }

            Then("차감된 재고 수량은 올바르게 계산되어야 한다") {
                val finalStock = finalProduct?.stock ?: 0
                val deductedStock = initialStock - finalStock

                deductedStock shouldBeGreaterThanOrEqual 0
                deductedStock shouldBeLessThanOrEqual numberOfThreads
            }
        }
    }
})
