package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) : BehaviorSpec() {

    @MockkBean
    private lateinit var productService: ProductService

    @MockkBean
    private lateinit var productRankingService: ProductRankingService

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("상품 목록 조회 API") {
            When("기본 페이지네이션으로 상품 목록을 조회하면") {
                clearMocks(productService)
                
                val now = LocalDateTime.now()
                val mockProducts = listOf(
                    ProductDto.ProductInfo(
                        id = 1L,
                        name = "아이폰 15",
                        price = 1200000,
                        stock = 50,
                        createdAt = now.minusDays(10),
                        updatedAt = now.minusDays(1)
                    ),
                    ProductDto.ProductInfo(
                        id = 2L,
                        name = "갤럭시 S24",
                        price = 1000000,
                        stock = 30,
                        createdAt = now.minusDays(5),
                        updatedAt = now.minusHours(1)
                    )
                )
                
                val pageable = PageRequest.of(0, 10)
                val page = PageImpl(mockProducts, pageable, mockProducts.size.toLong())

                every { productService.getAllProducts(any()) } returns page

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(2))
                        .andExpect(jsonPath("$.data.products[0].id").value(1))
                        .andExpect(jsonPath("$.data.products[0].name").value("아이폰 15"))
                        .andExpect(jsonPath("$.data.products[0].price").value(1200000))
                        .andExpect(jsonPath("$.data.products[0].stock").value(50))
                        .andExpect(jsonPath("$.data.products[1].id").value(2))
                        .andExpect(jsonPath("$.data.products[1].name").value("갤럭시 S24"))
                        .andExpect(jsonPath("$.data.pagination.page").value(0))
                        .andExpect(jsonPath("$.data.pagination.size").value(10))
                        .andExpect(jsonPath("$.data.pagination.totalElements").value(2))
                    
                    verify(exactly = 1) { productService.getAllProducts(any()) }
                }
            }

            When("검색 키워드로 상품을 조회하면") {
                clearMocks(productService)
                
                val searchKeyword = "아이폰"
                val now = LocalDateTime.now()
                val mockProducts = listOf(
                    ProductDto.ProductInfo(
                        id = 1L,
                        name = "아이폰 15",
                        price = 1200000,
                        stock = 50,
                        createdAt = now.minusDays(10),
                        updatedAt = now.minusDays(1)
                    ),
                    ProductDto.ProductInfo(
                        id = 3L,
                        name = "아이폰 14",
                        price = 1000000,
                        stock = 20,
                        createdAt = now.minusDays(20),
                        updatedAt = now.minusDays(5)
                    )
                )
                
                val pageable = PageRequest.of(0, 10)
                val page = PageImpl(mockProducts, pageable, mockProducts.size.toLong())

                every { productService.searchProductsByName(any(), eq(searchKeyword)) } returns page

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("search", searchKeyword)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 검색된 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(2))
                        .andExpect(jsonPath("$.data.products[0].name").value("아이폰 15"))
                        .andExpect(jsonPath("$.data.products[1].name").value("아이폰 14"))
                    
                    verify(exactly = 1) { productService.searchProductsByName(any(), eq(searchKeyword)) }
                }
            }

            When("가격 범위로 상품을 조회하면") {
                clearMocks(productService)
                
                val minPrice = 500000
                val maxPrice = 1000000
                val now = LocalDateTime.now()
                val mockProducts = listOf(
                    ProductDto.ProductInfo(
                        id = 2L,
                        name = "갤럭시 S24",
                        price = 1000000,
                        stock = 30,
                        createdAt = now.minusDays(5),
                        updatedAt = now.minusHours(1)
                    )
                )
                
                val pageable = PageRequest.of(0, 10)
                val page = PageImpl(mockProducts, pageable, mockProducts.size.toLong())

                every { productService.getProductsByPriceRange(any(), eq(minPrice), eq(maxPrice)) } returns page

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("minPrice", minPrice.toString())
                        .param("maxPrice", maxPrice.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 가격 범위에 맞는 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(1))
                        .andExpect(jsonPath("$.data.products[0].name").value("갤럭시 S24"))
                        .andExpect(jsonPath("$.data.products[0].price").value(1000000))
                    
                    verify(exactly = 1) { productService.getProductsByPriceRange(any(), eq(minPrice), eq(maxPrice)) }
                }
            }

            When("검색 결과가 없는 키워드로 조회하면") {
                clearMocks(productService)
                
                val searchKeyword = "존재하지않는상품"
                val pageable = PageRequest.of(0, 10)
                val emptyPage = PageImpl<ProductDto.ProductInfo>(emptyList(), pageable, 0)

                every { productService.searchProductsByName(any(), eq(searchKeyword)) } returns emptyPage

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("search", searchKeyword)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(0))
                        .andExpect(jsonPath("$.data.pagination.totalElements").value(0))
                    
                    verify(exactly = 1) { productService.searchProductsByName(any(), eq(searchKeyword)) }
                }
            }

            When("커스텀 페이지 크기로 상품을 조회하면") {
                clearMocks(productService)
                
                val now = LocalDateTime.now()
                val mockProducts = listOf(
                    ProductDto.ProductInfo(
                        id = 1L,
                        name = "상품1",
                        price = 10000,
                        stock = 100,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                
                val pageable = PageRequest.of(0, 5)
                val page = PageImpl(mockProducts, pageable, mockProducts.size.toLong())

                every { productService.getAllProducts(any()) } returns page

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 요청된 페이지 크기의 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.pagination.size").value(5))
                        .andExpect(jsonPath("$.data.pagination.page").value(0))
                    
                    verify(exactly = 1) { productService.getAllProducts(any()) }
                }
            }
        }

        Given("상품 상세 조회 API") {
            When("존재하는 상품 ID로 상세 정보를 조회하면") {
                clearMocks(productService)
                
                val productId = 1L
                val now = LocalDateTime.now()
                val mockProduct = ProductDto.ProductInfo(
                    id = productId,
                    name = "아이폰 15 Pro",
                    price = 1500000,
                    stock = 25,
                    createdAt = now.minusDays(7),
                    updatedAt = now.minusHours(2)
                )

                every { productService.getProduct(productId) } returns mockProduct

                val result = mockMvc.perform(
                    get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 상품 상세 정보를 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").value(productId))
                        .andExpect(jsonPath("$.data.name").value("아이폰 15 Pro"))
                        .andExpect(jsonPath("$.data.price").value(1500000))
                        .andExpect(jsonPath("$.data.stock").value(25))
                        .andExpect(jsonPath("$.data.createdAt").exists())
                        .andExpect(jsonPath("$.data.updatedAt").exists())
                    
                    verify(exactly = 1) { productService.getProduct(productId) }
                }
            }

            When("존재하지 않는 상품 ID로 상세 정보를 조회하면") {
                clearMocks(productService)
                
                val productId = 999L

                every { productService.getProduct(productId) } throws BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

                val result = mockMvc.perform(
                    get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("404 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isNotFound)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
                    
                    verify(exactly = 1) { productService.getProduct(productId) }
                }
            }

            When("잘못된 형식의 상품 ID로 조회하면") {
                clearMocks(productService)
                
                val result = mockMvc.perform(
                    get("/api/v1/products/{productId}", "invalid_id")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("음수 상품 ID로 조회하면") {
                clearMocks(productService)
                
                val productId = -1L

                every { productService.getProduct(productId) } throws BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

                val result = mockMvc.perform(
                    get("/api/v1/products/{productId}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("404 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isNotFound)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
                    
                    verify(exactly = 1) { productService.getProduct(productId) }
                }
            }
        }

        Given("상품 검색 및 필터링") {
            When("빈 검색어로 검색하면") {
                clearMocks(productService)
                
                val now = LocalDateTime.now()
                val mockProducts = listOf(
                    ProductDto.ProductInfo(
                        id = 1L,
                        name = "기본 상품",
                        price = 50000,
                        stock = 10,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                
                val pageable = PageRequest.of(0, 10)
                val page = PageImpl(mockProducts, pageable, mockProducts.size.toLong())

                every { productService.getAllProducts(any()) } returns page

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("search", "")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 전체 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                    
                    verify(exactly = 1) { productService.getAllProducts(any()) }
                }
            }

            When("잘못된 가격 범위로 조회하면 (minPrice > maxPrice)") {
                clearMocks(productService)
                
                val minPrice = 1000000
                val maxPrice = 500000
                val pageable = PageRequest.of(0, 10)
                val emptyPage = PageImpl<ProductDto.ProductInfo>(emptyList(), pageable, 0)

                every { productService.getProductsByPriceRange(any(), eq(minPrice), eq(maxPrice)) } returns emptyPage

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("minPrice", minPrice.toString())
                        .param("maxPrice", maxPrice.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(0))
                    
                    verify(exactly = 1) { productService.getProductsByPriceRange(any(), eq(minPrice), eq(maxPrice)) }
                }
            }

            When("음수 가격으로 조회하면") {
                clearMocks(productService)
                
                val minPrice = -1000
                val maxPrice = 50000
                val pageable = PageRequest.of(0, 10)
                val emptyPage = PageImpl<ProductDto.ProductInfo>(emptyList(), pageable, 0)

                every { productService.getProductsByPriceRange(any(), eq(minPrice), eq(maxPrice)) } returns emptyPage

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("minPrice", minPrice.toString())
                        .param("maxPrice", maxPrice.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(0))
                    
                    verify(exactly = 1) { productService.getProductsByPriceRange(any(), eq(minPrice), eq(maxPrice)) }
                }
            }

            When("잘못된 형식의 가격 파라미터로 조회하면") {
                clearMocks(productService)
                
                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("minPrice", "invalid_price")
                        .param("maxPrice", "not_number")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("minPrice만 제공하고 maxPrice는 누락하면") {
                clearMocks(productService)
                
                val now = LocalDateTime.now()
                val mockProducts = listOf(
                    ProductDto.ProductInfo(
                        id = 1L,
                        name = "상품",
                        price = 50000,
                        stock = 10,
                        createdAt = now,
                        updatedAt = now
                    )
                )
                
                val pageable = PageRequest.of(0, 10)
                val page = PageImpl(mockProducts, pageable, mockProducts.size.toLong())

                every { productService.getAllProducts(any()) } returns page

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("minPrice", "50000")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 전체 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                    
                    verify(exactly = 1) { productService.getAllProducts(any()) }
                }
            }
        }

        Given("페이지네이션 테스트") {
            When("큰 페이지 번호로 조회하면") {
                clearMocks(productService)
                
                val pageable = PageRequest.of(100, 10)
                val emptyPage = PageImpl<ProductDto.ProductInfo>(emptyList(), pageable, 0)

                every { productService.getAllProducts(any()) } returns emptyPage

                val result = mockMvc.perform(
                    get("/api/v1/products")
                        .param("page", "100")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(0))
                        .andExpect(jsonPath("$.data.pagination.page").value(100))
                    
                    verify(exactly = 1) { productService.getAllProducts(any()) }
                }
            }
        }
    }
}
