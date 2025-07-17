package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.presentation.dto.request.BalanceChargeRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(BalanceController::class)
class BalanceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var balanceService: BalanceService

    @Test
    fun `잔액 충전 API는 성공 응답 반환`() {
        // given
        val request = BalanceChargeRequest(userId = 1L, amount = 10000)
        every { balanceService.charge(1L, 10000) } returns true

        // when & then
        mockMvc.perform(post("/api/v1/balance/charge")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.chargedAmount").value(10000))
            .andExpect(jsonPath("$.data.balance").value(20000))
    }

    @Test
    fun `잔액 충전 실패 시 예외 응답 반환`() {
        // given
        val request = BalanceChargeRequest(userId = 1L, amount = 10000)
        every { balanceService.charge(1L, 10000) } returns false

        // when & then
        mockMvc.perform(post("/api/v1/balance/charge")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `잔액 조회 API는 현재 잔액 반환`() {
        // given
        every { balanceService.getBalance(1L) } returns 15000

        // when & then
        mockMvc.perform(get("/api/v1/balance")
            .param("userId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.balance").value(15000))
    }

    @Test
    fun `잔액이 없는 사용자 조회 시 0원 반환`() {
        // given
        every { balanceService.getBalance(999L) } returns 0

        // when & then
        mockMvc.perform(get("/api/v1/balance")
            .param("userId", "999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(999L))
            .andExpect(jsonPath("$.data.balance").value(0))
    }
}
