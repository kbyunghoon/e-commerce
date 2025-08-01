package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.model.Product
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(ProductController::class)
class ProductControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var productService: ProductService

    @Test
    fun `전체 상품 조회 API는 mock 리스트 반환`() {
        // given
        val mockProducts = listOf(
            Product(1L, "아이폰", 1000, 100, LocalDateTime.now(), LocalDateTime.now()),
            Product(2L, "갤럭시", 2000, 200, LocalDateTime.now(), LocalDateTime.now())
        )
        every { productService.getProducts(0, 10, null, null, null) } returns mockProducts

        // when & then
        mockMvc.perform(get("/api/v1/products")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.products[0].id").value(1L))
            .andExpect(jsonPath("$.data.products[0].name").value("아이폰"))
            .andExpect(jsonPath("$.data.products[0].price").value(1000))
            .andExpect(jsonPath("$.data.products[0].stock").value(100))
            .andExpect(jsonPath("$.data.products[1].id").value(2L))
            .andExpect(jsonPath("$.data.products[1].name").value("갤럭시"))
            .andExpect(jsonPath("$.data.products[1].price").value(2000))
            .andExpect(jsonPath("$.data.products[1].stock").value(200))
    }

    @Test
    fun `상품 상세 조회 API는 단일 상품 반환`() {
        // given
        val mockProduct = Product(1L, "아이폰", 1000, 100, LocalDateTime.now(), LocalDateTime.now())
        every { productService.getProduct(1L) } returns mockProduct

        // when & then
        mockMvc.perform(get("/api/v1/products/1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.id").value(1L))
            .andExpect(jsonPath("$.data.name").value("아이폰"))
            .andExpect(jsonPath("$.data.price").value(1000))
            .andExpect(jsonPath("$.data.stock").value(100))
    }

    @Test
    fun `존재하지 않는 상품 조회 시 404 반환`() {
        // given
        every { productService.getProduct(999L) } returns null

        // when & then
        mockMvc.perform(get("/api/v1/products/999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound)
    }

    @Test
    fun `상품 검색 API는 필터링된 결과 반환`() {
        // given
        val mockProducts = listOf(
            Product(1L, "아이폰", 1000, 100, LocalDateTime.now(), LocalDateTime.now())
        )
        every { productService.getProducts(0, 10, "아이폰", null, null) } returns mockProducts

        // when & then
        mockMvc.perform(get("/api/v1/products")
            .param("search", "아이폰")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.products").isArray)
            .andExpect(jsonPath("$.data.products[0].name").value("아이폰"))
    }
}
