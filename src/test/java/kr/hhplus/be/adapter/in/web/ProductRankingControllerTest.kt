package kr.hhplus.be.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.dto.ProductRankingInfo
import kr.hhplus.be.application.port.`in`.ProductRankingUseCase
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
    private lateinit var productRankingUseCase: ProductRankingUseCase

    @Test
    fun `인기 상품 조회 API는 상위 5개 상품 반환`() {
        // given
        val mockTopProducts = listOf(
            ProductRankingInfo(1L, "아이폰", 1, 95.5),
            ProductRankingInfo(2L, "갤럭시", 2, 92.3),
            ProductRankingInfo(3L, "맥북", 3, 88.0),
            ProductRankingInfo(4L, "아이패드", 4, 85.0),
            ProductRankingInfo(5L, "에어팟", 5, 80.0)
        )
        
        every { productRankingUseCase.getTopProducts(any()) } returns mockTopProducts

        // when & then
        mockMvc.perform(get("/api/v1/products/top")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.products").isArray)
            .andExpect(jsonPath("$.data.products.length()").value(5))
            .andExpect(jsonPath("$.data.products[0].id").value(1L))
            .andExpect(jsonPath("$.data.products[0].name").value("아이폰"))
            .andExpect(jsonPath("$.data.products[0].price").value(0))
            .andExpect(jsonPath("$.data.products[0].totalSalesQuantity").value(0))
            .andExpect(jsonPath("$.data.products[0].rank").value(1))
            .andExpect(jsonPath("$.data.products[1].name").value("갤럭시"))
            .andExpect(jsonPath("$.data.products[2].name").value("맥북"))
            .andExpect(jsonPath("$.data.products[3].name").value("아이패드"))
            .andExpect(jsonPath("$.data.products[4].name").value("에어팟"))
    }

    @Test
    fun `판매 데이터가 없는 경우 빈 목록 반환`() {
        // given
        every { productRankingUseCase.getTopProducts(any()) } returns emptyList()

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
            ProductRankingInfo(1L, "아이폰", 1, 95.5),
            ProductRankingInfo(2L, "갤럭시", 2, 92.3),
            ProductRankingInfo(3L, "맥북", 3, 88.0)
        )
        
        every { productRankingUseCase.getTopProducts(any()) } returns mockTopProducts

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
