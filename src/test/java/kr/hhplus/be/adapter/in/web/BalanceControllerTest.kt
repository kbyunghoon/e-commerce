package kr.hhplus.be.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.dto.BalanceChargeCommand
import kr.hhplus.be.application.dto.BalanceInfo
import kr.hhplus.be.application.port.`in`.BalanceUseCase
import kr.hhplus.be.adapter.`in`.web.dto.request.BalanceChargeRequest
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@WebMvcTest(BalanceController::class)
class BalanceControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var balanceUseCase: BalanceUseCase

    @Test
    fun `잔액 충전 API는 성공 응답 반환`() {
        // given
        val request = BalanceChargeRequest(userId = 1L, amount = 10000)
        val command = BalanceChargeCommand(userId = 1L, amount = 10000)
        val result = BalanceInfo(1L, 1L, 30000, LocalDateTime.now(), LocalDateTime.now())

        every { balanceUseCase.charge(command) } returns result

        // when & then
        mockMvc.perform(post("/api/v1/balance/charge")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.chargedAmount").value(10000))
            .andExpect(jsonPath("$.data.balance").value(30000))
    }

    @Test
    fun `잔액 충전 실패 시 예외 응답 반환`() {
        // given
        val request = BalanceChargeRequest(userId = 1L, amount = -100)
        val command = BalanceChargeCommand(userId = 1L, amount = -100)

        every { balanceUseCase.charge(command) } throws BusinessException(ErrorCode.INVALID_INPUT_VALUE)

        // when & then
        mockMvc.perform(post("/api/v1/balance/charge")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
    }

    @Test
    fun `잔액 조회 API는 현재 잔액 반환`() {
        // given
        val result = BalanceInfo(1L, 1L, 15000, LocalDateTime.now(), LocalDateTime.now())
        every { balanceUseCase.getBalance(1L) } returns result

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
        val result = BalanceInfo(2L, 999L, 0, LocalDateTime.now(), LocalDateTime.now())
        every { balanceUseCase.getBalance(999L) } returns result

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
