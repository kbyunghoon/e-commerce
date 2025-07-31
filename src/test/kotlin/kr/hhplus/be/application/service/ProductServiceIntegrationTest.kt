package kr.hhplus.be.application.service

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.config.IntegrationTest
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductRepository
import kr.hhplus.be.domain.product.ProductStatus
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime

@IntegrationTest
class ProductServiceIntegrationTest @Autowired constructor(
    private val productService: ProductService,
    private val productRepository: ProductRepository
) : BehaviorSpec({

    Given("실제 MySQL 데이터베이스를 사용한 상품 서비스 통합 테스트") {

        When("상품을 생성하고 조회하면") {
            val now = LocalDateTime.now()
            val product = Product(
                id = 0L,
                name = "통합테스트 상품",
                stock = 50,
                price = 15000,
                createdAt = now,
                updatedAt = now,
                status = ProductStatus.ACTIVE
            )

            val savedProduct = productRepository.save(product)
            val retrievedProduct = productService.getProduct(savedProduct.id)

            Then("저장된 상품이 정확히 조회된다") {
                retrievedProduct.name shouldBe "통합테스트 상품"
                retrievedProduct.stock shouldBe 50
                retrievedProduct.price shouldBe 15000
            }
        }

        When("재고를 차감하면") {
            val now = LocalDateTime.now()
            val product = Product(
                id = 0L,
                name = "재고차감 테스트 상품",
                stock = 100,
                price = 10000,
                createdAt = now,
                updatedAt = now,
                status = ProductStatus.ACTIVE
            )

            val savedProduct = productRepository.save(product)
            productService.deductStock(savedProduct.id, 30)

            val updatedProduct = productService.getProduct(savedProduct.id)

            Then("재고가 정확히 차감된다") {
                updatedProduct.stock shouldBe 70
            }
        }

        When("여러 상품을 동시에 처리하면") {
            val now = LocalDateTime.now()
            val products = listOf(
                Product(0L, "상품1", 10, 1000, now, now, ProductStatus.ACTIVE),
                Product(0L, "상품2", 20, 2000, now, now, ProductStatus.ACTIVE),
                Product(0L, "상품3", 30, 3000, now, now, ProductStatus.ACTIVE)
            )

            val savedProducts = products.map { productRepository.save(it) }
            val productIds = savedProducts.map { it.id }

            val retrievedProducts = productService.getProductsByIds(productIds)

            Then("모든 상품이 정확히 조회된다") {
                retrievedProducts.size shouldBe 3
                retrievedProducts.map { it.name }.sorted() shouldBe listOf("상품1", "상품2", "상품3")
            }
        }
    }
})
