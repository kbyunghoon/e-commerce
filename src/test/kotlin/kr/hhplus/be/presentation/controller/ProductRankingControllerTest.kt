package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.facade.ProductRankingFacade
import kr.hhplus.be.application.product.ProductRankingInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class ProductRankingControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) : BehaviorSpec() {

    @MockkBean
    private lateinit var productRankingFacade: ProductRankingFacade

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("인기 상품 조회 API") {
            When("인기 상품 목록을 조회하면") {
                clearMocks(productRankingFacade)

                val mockProductRankings = listOf(
                    ProductRankingInfo(
                        productId = 1L,
                        productName = "인기 상품 1",
                        rank = 1,
                    ),
                    ProductRankingInfo(
                        productId = 2L,
                        productName = "인기 상품 2",
                        rank = 2,
                    ),
                    ProductRankingInfo(
                        productId = 3L,
                        productName = "인기 상품 3",
                        rank = 3,
                    ),
                    ProductRankingInfo(
                        productId = 4L,
                        productName = "인기 상품 4",
                        rank = 4,
                    ),
                    ProductRankingInfo(
                        productId = 5L,
                        productName = "인기 상품 5",
                        rank = 5,
                    )
                )

                every { productRankingFacade.getTopProducts() } returns mockProductRankings

                val result = mockMvc.perform(
                    get("/api/v1/products/top")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 인기 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(5))
                        .andExpect(jsonPath("$.data.products[0].productId").value(1))
                        .andExpect(jsonPath("$.data.products[0].productName").value("인기 상품 1"))
                        .andExpect(jsonPath("$.data.products[0].rank").value(1))
                        .andExpect(jsonPath("$.data.products[1].rank").value(2))
                        .andExpect(jsonPath("$.data.products[4].rank").value(5))

                    verify(exactly = 1) { productRankingFacade.getTopProducts() }
                }
            }

            When("인기 상품이 없을 때 조회하면") {
                clearMocks(productRankingFacade)

                val emptyList = emptyList<ProductRankingInfo>()

                every { productRankingFacade.getTopProducts() } returns emptyList

                val result = mockMvc.perform(
                    get("/api/v1/products/top")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 상품 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.products").isArray)
                        .andExpect(jsonPath("$.data.products.length()").value(0))

                    verify(exactly = 1) { productRankingFacade.getTopProducts() }
                }
            }
        }
    }
}
