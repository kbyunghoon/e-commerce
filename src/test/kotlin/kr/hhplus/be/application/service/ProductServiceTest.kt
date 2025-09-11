package kr.hhplus.be.application.service

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.application.product.ProductSearchCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.*
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import java.time.LocalDateTime

class ProductServiceTest : BehaviorSpec({
    val productRepository: ProductRepository = mockk()
    val productStockHistoryService: ProductStockHistoryService = mockk()
    val productStockHistoryRepository: ProductStockHistoryRepository = mockk()
    val productRankingService: ProductRankingService = mockk()
    val productRankingRepository: ProductRankingRepository = mockk()
    val productService = ProductService(
        productRepository,
        productStockHistoryRepository,
        productStockHistoryService,
        productRankingService,
        productRankingRepository
    )

    afterContainer {
        clearAllMocks()
    }

    Given("상품 상세 조회(getProduct) 시나리오") {
        val productId = 1L
        val now = LocalDateTime.now()
        val product = Product(
            id = productId,
            name = "상품 테스트",
            stock = 100,
            price = 10000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )

        When("존재하는 상품 ID로 조회를 요청하면") {
            every { productRepository.findByIdOrThrow(productId) } returns product

            val result = productService.getProduct(productId)

            Then("해당 상품 정보가 반환된다") {
                result.id shouldBe productId
                result.name shouldBe "상품 테스트"
                verify(exactly = 1) { productRepository.findByIdOrThrow(productId) }
            }
        }

        When("존재하지 않는 상품 ID로 조회를 요청하면") {
            every { productRepository.findByIdOrThrow(productId) } throws BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                productService.getProduct(productId)
            }

            Then("PRODUCT_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
                verify(exactly = 1) { productRepository.findByIdOrThrow(productId) }
            }
        }
    }

    Given("재고 차감(deductStock) 시나리오") {
        val productId = 1L
        val quantity = 5
        val now = LocalDateTime.now()
        val product = Product(
            id = productId,
            name = "상품 테스트",
            stock = 10,
            price = 10000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )

        When("충분한 재고가 있는 상품의 재고 차감을 요청하면") {
            val updatedProduct = Product(
                id = productId,
                name = "상품 테스트",
                stock = 5,
                price = 10000,
                status = ProductStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
            )
            val command = ProductStockHistory.create(
                productId = productId,
                changeType = StockChangeType.DEDUCT,
                changeQuantity = updatedProduct.stock,
                previousStock = product.stock,
                transactionAt = updatedProduct.updatedAt
            )

            every { productRepository.findByIdWithPessimisticLock(productId) } returns product
            every { productRepository.save(any()) } returns updatedProduct
            every { productStockHistoryService.save(any()) } returns command
            every { productRankingService.updateSalesCount(any(), any()) } returns Unit

            val result = productService.deductStock(productId, quantity)

            Then("상품의 재고가 감소하고, 재고 변경 이벤트가 발행된다") {
                result.stock shouldBe 5
                verify(exactly = 1) { productRepository.findByIdWithPessimisticLock(productId) }
                verify(exactly = 1) { productRepository.save(any()) }
                verify(exactly = 1) { productStockHistoryService.save(any()) }
                verify(exactly = 1) { productRankingService.updateSalesCount(any(), any()) }
            }
        }

        When("재고가 부족한 상품의 재고 차감을 요청하면") {
            val insufficientStockProduct = Product(
                id = productId,
                name = "상품 테스트",
                stock = 3,
                price = 10000,
                status = ProductStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
            )
            every { productRepository.findByIdWithPessimisticLock(productId) } returns insufficientStockProduct

            val exception = shouldThrow<BusinessException> {
                productService.deductStock(productId, quantity)
            }

            Then("INSUFFICIENT_STOCK 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.INSUFFICIENT_STOCK
                verify(exactly = 1) { productRepository.findByIdWithPessimisticLock(productId) }
                verify(exactly = 0) { productRepository.save(any()) }
            }
        }

        When("존재하지 않는 상품의 재고 차감을 요청하면") {
            every { productRepository.findByIdWithPessimisticLock(productId) } throws BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            val exception = shouldThrow<BusinessException> {
                productService.deductStock(productId, quantity)
            }

            Then("PRODUCT_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
                verify(exactly = 1) { productRepository.findByIdWithPessimisticLock(productId) }
                verify(exactly = 0) { productRepository.save(any()) }
            }
        }
    }

    Given("재고 복원(restoreStock) 시나리오") {
        val productId = 1L
        val quantity = 5
        val now = LocalDateTime.now()
        val product = Product(
            id = productId,
            name = "상품 테스트",
            stock = 10,
            price = 10000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )

        When("재고 복원을 요청하면") {
            val updatedProduct = Product(
                id = productId,
                name = "상품 테스트",
                stock = 15,
                price = 10000,
                status = ProductStatus.ACTIVE,
                createdAt = now,
                updatedAt = now,
            )
            val command = ProductStockHistory.create(
                productId = productId,
                changeType = StockChangeType.DEDUCT,
                changeQuantity = updatedProduct.stock,
                previousStock = product.stock,
                transactionAt = updatedProduct.updatedAt
            )

            every { productRepository.findByIdOrThrow(productId) } returns product
            every { productRepository.save(any()) } returns updatedProduct
            every { productStockHistoryService.save(any()) } returns command
            every { productRankingService.updateSalesCount(any(), any()) } returns Unit

            val result = productService.restoreStock(productId, quantity)

            Then("상품의 재고가 증가하고, 재고 변경 이벤트가 발행된다") {
                result.stock shouldBe 15
                verify(exactly = 1) { productRepository.findByIdOrThrow(productId) }
                verify(exactly = 1) { productRepository.save(any()) }
                verify(exactly = 1) { productStockHistoryService.save(any()) }
                verify(exactly = 1) { productRankingService.updateSalesCount(any(), any()) }
            }
        }
    }

    Given("주문 아이템 검증(validateOrderItems) 시나리오") {
        val productId1 = 1L
        val productId2 = 2L
        val now = LocalDateTime.now()
        val product1 = Product(
            id = productId1,
            name = "상품 A",
            stock = 10,
            price = 1000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        val product2 = Product(
            id = productId2,
            name = "상품 B",
            stock = 5,
            price = 2000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )

        When("모든 주문 아이템이 유효하고 재고가 충분하면") {
            val items = listOf(
                OrderItemCreateCommand(productId1, 2),
                OrderItemCreateCommand(productId2, 3)
            )
            every { productRepository.findByProductIds(listOf(productId1, productId2)) } returns listOf(
                product1,
                product2
            )

            val result = productService.validateOrderItems(items)

            Then("ProductInfo 리스트가 반환된다") {
                result.size shouldBe 2
                result[0].id shouldBe productId1
                result[1].id shouldBe productId2
                verify(exactly = 1) { productRepository.findByProductIds(listOf(productId1, productId2)) }
            }
        }

        When("존재하지 않는 상품이 주문 아이템에 포함되어 있으면") {
            val items = listOf(
                OrderItemCreateCommand(productId1, 2),
                OrderItemCreateCommand(999L, 1)
            )
            every { productRepository.findByProductIds(listOf(productId1, 999L)) } returns listOf(product1)

            val exception = shouldThrow<BusinessException> {
                productService.validateOrderItems(items)
            }

            Then("PRODUCT_NOT_FOUND 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.PRODUCT_NOT_FOUND
                verify(exactly = 1) { productRepository.findByProductIds(listOf(productId1, 999L)) }
            }
        }

        When("재고가 부족한 상품이 주문 아이템에 포함되어 있으면") {
            val items = listOf(
                OrderItemCreateCommand(productId1, 2),
                OrderItemCreateCommand(productId2, 10)
            )
            every { productRepository.findByProductIds(listOf(productId1, productId2)) } returns listOf(
                product1,
                product2
            )

            val exception = shouldThrow<BusinessException> {
                productService.validateOrderItems(items)
            }

            Then("INSUFFICIENT_STOCK 예외가 발생한다") {
                exception.errorCode shouldBe ErrorCode.INSUFFICIENT_STOCK
                verify(exactly = 1) { productRepository.findByProductIds(listOf(productId1, productId2)) }
            }
        }
    }

    Given("페이징된 상품 목록 조회(getProducts) 시나리오") {
        val pageable: Pageable = PageRequest.of(0, 10)
        val now = LocalDateTime.now()
        val product1 = Product(
            id = 1L,
            name = "상품 A",
            stock = 10,
            price = 1000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        val product2 = Product(
            id = 2L,
            name = "상품 B",
            stock = 20,
            price = 2000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        val productsPage = PageImpl(listOf(product1, product2), pageable, 2)

        When("검색 조건 없이 상품 목록 조회를 요청하면") {
            every { productRepository.findAvailableProducts(pageable, null, null, null) } returns productsPage
            val command = ProductSearchCommand(pageable, null, null, null)

            val result = productService.getProducts(command)

            Then("페이징된 상품 정보가 반환된다") {
                result.content.size shouldBe 2
                result.totalElements shouldBe 2
                result.totalPages shouldBe 1
                verify(exactly = 1) { productRepository.findAvailableProducts(pageable, null, null, null) }
            }
        }

        When("검색 키워드를 포함하여 상품 목록 조회를 요청하면") {
            val searchKeyword = "상품 A"
            val filteredProductsPage = PageImpl(listOf(product1), pageable, 1)
            every {
                productRepository.findAvailableProducts(
                    any(), any(), any(), any()
                )
            } returns filteredProductsPage

            val command = ProductSearchCommand(pageable, null, null, null)

            val result = productService.getProducts(command)

            Then("검색 키워드에 해당하는 페이징된 상품 정보가 반환된다") {
                result.content.size shouldBe 1
                result.content[0].name shouldBe "상품 A"
                verify(exactly = 1) { productRepository.findAvailableProducts(any(), any(), any(), any()) }
            }
        }

        When("가격 범위를 포함하여 상품 목록 조회를 요청하면") {
            val minPrice = 500
            val maxPrice = 1500
            val filteredProductsPage = PageImpl(listOf(product1), pageable, 1)
            every {
                productRepository.findAvailableProducts(any(), any(), any(), any())
            } returns filteredProductsPage

            val command = ProductSearchCommand(pageable, null, null, null)
            val result = productService.getProducts(command)

            Then("가격 범위에 해당하는 페이징된 상품 정보가 반환된다") {
                result.content.size shouldBe 1
                result.content[0].price shouldBe 1000
                verify(exactly = 1) { productRepository.findAvailableProducts(any(), any(), any(), any()) }
            }
        }
    }

    Given("모든 상품 목록 조회(getAllProducts) 시나리오") {
        val pageable: Pageable = PageRequest.of(0, 10)
        val now = LocalDateTime.now()
        val product1 = Product(
            id = 1L,
            name = "상품 A",
            stock = 10,
            price = 1000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        val product2 = Product(
            id = 2L,
            name = "상품 B",
            stock = 20,
            price = 2000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        val productsPage = PageImpl(listOf(product1, product2), pageable, 2)

        When("모든 상품 목록 조회를 요청하면") {
            every { productRepository.findAll(pageable) } returns productsPage

            val result = productService.getAllProducts(pageable)

            Then("모든 상품 정보가 페이징되어 반환된다") {
                result.content.size shouldBe 2
                result.totalElements shouldBe 2
                verify(exactly = 1) { productRepository.findAll(pageable) }
            }
        }
    }

    Given("여러 상품 ID로 조회(getProductsByIds) 시나리오") {
        val productIds = listOf(1L, 2L)
        val now = LocalDateTime.now()
        val product1 = Product(
            id = 1L,
            name = "상품 A",
            stock = 10,
            price = 1000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )
        val product2 = Product(
            id = 2L,
            name = "상품 B",
            stock = 20,
            price = 2000,
            status = ProductStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )

        When("유효한 상품 ID 목록으로 조회를 요청하면") {
            every { productRepository.findByProductIds(productIds) } returns listOf(product1, product2)

            val result = productService.getProductsByIds(productIds)

            Then("해당 상품 정보 리스트가 반환된다") {
                result.size shouldBe 2
                result[0].id shouldBe 1L
                result[1].id shouldBe 2L
                verify(exactly = 1) { productRepository.findByProductIds(productIds) }
            }
        }

        When("일부 상품 ID가 존재하지 않는 경우 조회를 요청하면") {
            val invalidProductIds = listOf(1L, 999L)
            every { productRepository.findByProductIds(invalidProductIds) } returns listOf(product1)

            val result = productService.getProductsByIds(invalidProductIds)

            Then("존재하는 상품 정보만 반환된다") {
                result.size shouldBe 1
                result[0].id shouldBe 1L
                verify(exactly = 1) { productRepository.findByProductIds(invalidProductIds) }
            }
        }
    }
})
