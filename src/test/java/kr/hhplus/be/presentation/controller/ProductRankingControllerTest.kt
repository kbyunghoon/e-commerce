package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.service.ProductRankingService
import kr.hhplus.be.domain.model.ProductRanking
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(ProductRankingController::class)
class ProductRankingControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var productRankingService: ProductRankingService

    @Test
    fun `인기 상품 조회 API는 상위 5개 상품 반환`() {
        // given
        val mockTopProducts = listOf(
            ProductRanking(1L, "아이폰", 100000, 50, 1),
            ProductRanking(2L, "갤럭시", 90000, 30, 2),
            ProductRanking(3L, "맥북", 200000, 20, 3),
            ProductRanking(4L, "아이패드", 80000, 40, 4),
            ProductRanking(5L, "에어팟", 30000, 100, 5)
        )
        
        every { productRankingService.getTopProducts() } returns mockTopProducts

        // when & then
        mockMvc.perform(get("/api/v1/products/top")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.products").isArray)
            .andExpect(jsonPath("$.data.products.length()").value(5))
            .andExpect(jsonPath("$.data.products[0].id").value(1L))
            .andExpect(jsonPath("$.data.products[0].name").value("아이폰"))
            .andExpect(jsonPath("$.data.products[0].price").value(100000))
            .andExpect(jsonPath("$.data.products[0].totalSalesQuantity").value(50))
            .andExpect(jsonPath("$.data.products[0].rank").value(1))
            .andExpect(jsonPath("$.data.products[1].name").value("갤럭시"))
            .andExpect(jsonPath("$.data.products[2].name").value("맥북"))
            .andExpect(jsonPath("$.data.products[3].name").value("아이패드"))
            .andExpect(jsonPath("$.data.products[4].name").value("에어팟"))
    }

    @Test
    fun `판매 데이터가 없는 경우 빈 목록 반환`() {
        // given
        every { productRankingService.getTopProducts() } returns emptyList()

        // when & then
        mockMvc.perform(get("/api/v1/products/top")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.products").isArray)
            .andExpect(jsonPath("$.data.products.length()").value(0))
    }

    @Test
    @DisplayName("3개 상품만 있는 경우 3개만 반환")
    fun getTopProductsPartial() {
        // given
        val mockTopProducts = listOf(
            ProductRanking(1L, "아이폰", 100000, 50, 1),
            ProductRanking(2L, "갤럭시", 90000, 30, 2),
            ProductRanking(3L, "맥북", 200000, 20, 3)
        )
        
        every { productRankingService.getTopProducts() } returns mockTopProducts

        // when & then
        mockMvc.perform(get("/api/v1/products/top")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.products").isArray)
            .andExpect(jsonPath("$.data.products.length()").value(3))
            .andExpect(jsonPath("$.data.products[0].name").value("아이폰"))
            .andExpect(jsonPath("$.data.products[1].name").value("갤럭시"))
            .andExpect(jsonPath("$.data.products[2].name").value("맥북"))
    }
}
