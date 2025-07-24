package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.balance.BalanceInfo
import kr.hhplus.be.application.facade.BalanceFacade
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.presentation.dto.request.BalanceChargeRequest
import kr.hhplus.be.presentation.dto.response.BalanceChargeResponse
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
    private lateinit var balanceFacade: BalanceFacade

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

                val mockResponse = BalanceChargeResponse(
                    userId = userId,
                    balance = 15000,
                    chargedAmount = chargeAmount,
                    chargedAt = now
                )

                every { balanceFacade.chargeBalance(any()) } returns mockResponse

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
                        .andExpect(jsonPath("$.data.chargedAt").exists())

                    verify(exactly = 1) { balanceFacade.chargeBalance(any()) }
                }
            }

            When("0원으로 충전을 요청하면") {
                val userId = 1L
                val chargeAmount = 0
                val request = BalanceChargeRequest(userId, chargeAmount)

                every { balanceFacade.chargeBalance(any()) } throws BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)

                val result = mockMvc.perform(
                    post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("CHARGE_INVALID_AMOUNT"))

                    verify(exactly = 1) { balanceFacade.chargeBalance(any()) }
                }
            }

            When("음수 금액으로 충전을 요청하면") {
                val userId = 1L
                val chargeAmount = -1000
                val request = BalanceChargeRequest(userId, chargeAmount)

                every { balanceFacade.chargeBalance(any()) } throws BusinessException(ErrorCode.CHARGE_INVALID_AMOUNT)

                val result = mockMvc.perform(
                    post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("CHARGE_INVALID_AMOUNT"))

                    verify(exactly = 1) { balanceFacade.chargeBalance(any()) }
                }
            }

            When("존재하지 않는 사용자 ID로 충전을 요청하면") {
                val userId = 999L
                val chargeAmount = 10000
                val request = BalanceChargeRequest(userId, chargeAmount)

                every { balanceFacade.chargeBalance(any()) } throws BusinessException(ErrorCode.USER_NOT_FOUND)

                val result = mockMvc.perform(
                    post("/api/v1/balance/charge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("USER_NOT_FOUND"))
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
                        .andExpect(jsonPath("$.data.lastUpdatedAt").exists())

                    verify(exactly = 1) { balanceService.getBalance(userId) }
                }
            }

            When("잔액이 0인 사용자의 잔액을 조회하면") {
                val userId = 2L
                val now = LocalDateTime.now()
                val mockBalanceInfo = BalanceInfo(
                    id = 2L,
                    userId = userId,
                    amount = 0,
                    createdAt = now.minusDays(1),
                    updatedAt = now
                )

                every { balanceService.getBalance(userId) } returns mockBalanceInfo

                val result = mockMvc.perform(
                    get("/api/v1/balance")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 0원 잔액 정보를 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.balance").value(0))
                        .andExpect(jsonPath("$.data.lastUpdatedAt").exists())

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

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)

                    verify(exactly = 1) { balanceService.getBalance(userId) }
                }
            }

            When("userId 파라미터 없이 잔액을 조회하면") {
                val result = mockMvc.perform(
                    get("/api/v1/balance")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("잘못된 형식의 userId로 잔액을 조회하면") {
                val result = mockMvc.perform(
                    get("/api/v1/balance")
                        .param("userId", "invalid_user_id")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("INVALID_INPUT_VALUE"))
                }
            }
        }
    }
}
