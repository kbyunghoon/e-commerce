package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.product.ProductRankingDto
import kr.hhplus.be.application.service.ProductRankingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
class ProductRankingControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) : BehaviorSpec() {

    @MockkBean
    private lateinit var productRankingService: ProductRankingService

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("인기 상품 조회 API") {
            When("인기 상품 목록을 조회하면") {
                clearMocks(productRankingService)

                val mockProductRankings = listOf(
                    ProductRankingDto.ProductRankingInfo(
                        id = 1L,
                        productName = "인기 상품 1",
                        rank = 1,
                        totalSalesCount = 100,
                        rankingDate = LocalDate.now(),
                    ),
                    ProductRankingDto.ProductRankingInfo(
                        id = 2L,
                        productName = "인기 상품 2",
                        rank = 2,
                        totalSalesCount = 99,
                        rankingDate = LocalDate.now(),
                    ),
                    ProductRankingDto.ProductRankingInfo(
                        id = 3L,
                        productName = "인기 상품 3",
                        rank = 3,
                        totalSalesCount = 98,
                        rankingDate = LocalDate.now(),
                    ),
                    ProductRankingDto.ProductRankingInfo(
                        id = 4L,
                        productName = "인기 상품 4",
                        rank = 4,
                        totalSalesCount = 97,
                        rankingDate = LocalDate.now(),
                    ),
                    ProductRankingDto.ProductRankingInfo(
                        id = 5L,
                        productName = "인기 상품 5",
                        rank = 5,
                        totalSalesCount = 96,
                        rankingDate = LocalDate.now(),
                    )
                )

                every { productRankingService.getTopProducts(any()) } returns mockProductRankings

                val result = mockMvc.perform(
                    get("/api/v1/products/top")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 인기 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.rankings").isArray)
                        .andExpect(jsonPath("$.data.rankings.length()").value(5))
                        .andExpect(jsonPath("$.data.rankings[0].id").value(1))
                        .andExpect(jsonPath("$.data.rankings[0].productName").value("인기 상품 1"))
                        .andExpect(jsonPath("$.data.rankings[0].rank").value(1))
                        .andExpect(jsonPath("$.data.rankings[1].rank").value(2))
                        .andExpect(jsonPath("$.data.rankings[4].rank").value(5))

                    verify(exactly = 1) { productRankingService.getTopProducts(any()) }
                }
            }

            When("인기 상품이 없을 때 조회하면") {
                clearMocks(productRankingService)

                val emptyList = emptyList<ProductRankingDto.ProductRankingInfo>()

                every { productRankingService.getTopProducts(any()) } returns emptyList

                val result = mockMvc.perform(
                    get("/api/v1/products/top")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.rankings").isArray)
                        .andExpect(jsonPath("$.data.rankings.length()").value(0))

                    verify(exactly = 1) { productRankingService.getTopProducts(any()) }
                }
            }
        }
    }
}
