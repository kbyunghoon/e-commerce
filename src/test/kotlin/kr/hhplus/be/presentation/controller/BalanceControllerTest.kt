package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.balance.BalanceDto.BalanceInfo
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.presentation.dto.request.BalanceChargeRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class BalanceControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) : BehaviorSpec() {

    @MockkBean
    private lateinit var balanceService: BalanceService

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("잔액 충전 API") {
            When("유효한 사용자 ID와 금액으로 충전을 요청하면") {
                val userId = 1L
                val chargeAmount = 10000
                val request = BalanceChargeRequest(userId, chargeAmount)
                val now = LocalDateTime.now()

                val mockBalanceInfo = BalanceInfo(
                    id = 1L,
                    userId = userId,
                    amount = 15000,
                    createdAt = now.minusDays(1),
                    updatedAt = now
                )

                every { balanceService.charge(any()) } returns mockBalanceInfo

                val result = mockMvc.perform(
                    post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("200 상태코드와 충전된 잔액 정보를 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.balance").value(15000))
                        .andExpect(jsonPath("$.data.chargedAmount").value(chargeAmount))

                    verify(exactly = 1) { balanceService.charge(any()) }
                }
            }

            When("0원으로 충전을 요청하면") {
                val userId = 1L
                val chargeAmount = 0
                val request = BalanceChargeRequest(userId, chargeAmount)

                val result = mockMvc.perform(
                    post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))

                    verify(exactly = 0) { balanceService.charge(any()) }
                }
            }

            When("존재하지 않는 사용자 ID로 충전을 요청하면") {
                val userId = 999L
                val chargeAmount = 10000
                val request = BalanceChargeRequest(userId, chargeAmount)

                every { balanceService.charge(any()) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

                val result = mockMvc.perform(
                    post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))

                    verify(exactly = 1) { balanceService.charge(any()) }
                }
            }
        }

        Given("잔액 조회 API") {
            When("존재하는 사용자 ID로 잔액을 조회하면") {
                val userId = 1L
                val now = LocalDateTime.now()
                val mockBalanceInfo = BalanceInfo(
                    id = 1L,
                    userId = userId,
                    amount = 5000,
                    createdAt = now.minusDays(2),
                    updatedAt = now.minusHours(1)
                )

                every { balanceService.getBalance(userId) } returns mockBalanceInfo

                val result = mockMvc.perform(
                    get("/api/v1/balance")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 사용자 잔액 정보를 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.balance").value(5000))

                    verify(exactly = 1) { balanceService.getBalance(userId) }
                }
            }

            When("존재하지 않는 사용자 ID로 잔액을 조회하면") {
                val userId = 999L

                every { balanceService.getBalance(userId) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

                val result = mockMvc.perform(
                    get("/api/v1/balance")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))

                    verify(exactly = 1) { balanceService.getBalance(userId) }
                }
            }
        }
    }
}
