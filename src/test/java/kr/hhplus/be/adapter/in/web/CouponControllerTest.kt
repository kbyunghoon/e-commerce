package kr.hhplus.be.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.dto.CouponInfo
import kr.hhplus.be.application.dto.CouponIssueCommand
import kr.hhplus.be.application.port.`in`.CouponUseCase
import kr.hhplus.be.domain.enums.CouponStatus
import kr.hhplus.be.domain.enums.DiscountType
import kr.hhplus.be.adapter.`in`.web.dto.request.CouponIssueRequest
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
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(CouponController::class)
class CouponControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var couponUseCase: CouponUseCase

    @Test
    fun `쿠폰 발급 API는 성공 응답 반환`() {
        // given
        val request = CouponIssueRequest(
            userId = 1L,
            name = "10% 할인 쿠폰",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            issueCount = 1,
            expiredAt = LocalDate.now().plusDays(30)
        )
        val command = CouponIssueCommand(
            userId = 1L,
            name = "10% 할인 쿠폰",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            issueCount = 1,
            expiredAt = LocalDate.now().plusDays(30)
        )
        val result = CouponInfo(
            id = 1L,
            name = "10% 할인 쿠폰",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            status = CouponStatus.AVAILABLE,
            expiredAt = LocalDate.now().plusDays(30)
        )

        every { couponUseCase.issue(command) } returns result

        // when & then
        mockMvc.perform(post("/api/v1/coupons/issue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.userCouponId").value(1L))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.couponId").value(1L))
            .andExpect(jsonPath("$.data.couponName").value("10% 할인 쿠폰"))
    }

    @Test
    fun `쿠폰 발급 실패 시 예외 응답 반환`() {
        // given
        val request = CouponIssueRequest(
            userId = 1L,
            name = "SOLD_OUT_COUPON",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            issueCount = 1,
            expiredAt = LocalDate.now().plusDays(30)
        )
        val command = CouponIssueCommand(
            userId = 1L,
            name = "SOLD_OUT_COUPON",
            discountType = DiscountType.PERCENTAGE,
            discountValue = 10,
            issueCount = 1,
            expiredAt = LocalDate.now().plusDays(30)
        )

        every { couponUseCase.issue(command) } throws BusinessException(ErrorCode.COUPON_SOLD_OUT)

        // when & then
        mockMvc.perform(post("/api/v1/coupons/issue")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("COUPON_SOLD_OUT"))
    }

    @Test
    fun `보유 쿠폰 조회 API는 쿠폰 목록 반환`() {
        // given
        val mockCoupons = listOf(
            CouponInfo(
                id = 1L,
                name = "10% 할인 쿠폰",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10,
                status = CouponStatus.AVAILABLE,
                expiredAt = LocalDate.now().plusDays(30)
            ),
            CouponInfo(
                id = 2L,
                name = "5000원 할인 쿠폰",
                discountType = DiscountType.FIXED,
                discountValue = 5000,
                status = CouponStatus.USED,
                expiredAt = LocalDate.now().plusDays(30)
            )
        )
        every { couponUseCase.getCoupons(1L) } returns mockCoupons

        // when & then
        mockMvc.perform(get("/api/v1/coupons")
            .param("userId", "1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.coupons").isArray)
            .andExpect(jsonPath("$.data.coupons[0].userCouponId").value(1L))
            .andExpect(jsonPath("$.data.coupons[0].couponName").value("10% 할인 쿠폰"))
            .andExpect(jsonPath("$.data.coupons[0].discountType").value("PERCENTAGE"))
            .andExpect(jsonPath("$.data.coupons[0].discountValue").value(10))
            .andExpect(jsonPath("$.data.coupons[0].status").value("AVAILABLE"))
            .andExpect(jsonPath("$.data.coupons[1].status").value("USED"))
    }

    @Test
    fun `상태별 쿠폰 필터링 조회`() {
        // given
        val mockCoupons = listOf(
            CouponInfo(
                id = 1L,
                name = "10% 할인 쿠폰",
                discountType = DiscountType.PERCENTAGE,
                discountValue = 10,
                status = CouponStatus.AVAILABLE,
                expiredAt = LocalDate.now().plusDays(30)
            )
        )
        every { couponUseCase.getCoupons(1L) } returns mockCoupons

        // when & then
        mockMvc.perform(get("/api/v1/coupons")
            .param("userId", "1")
            .param("status", "AVAILABLE") // 이 부분은 CouponUseCase에서 처리하지 않으므로, 테스트에서 제거하거나 Mockk 설정 변경 필요
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.coupons").isArray)
            .andExpect(jsonPath("$.data.coupons[0].status").value("AVAILABLE"))
    }
}
