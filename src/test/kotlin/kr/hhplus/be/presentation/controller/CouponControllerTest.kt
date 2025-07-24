package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.coupon.UserCouponInfo
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.domain.coupon.CouponStatus
import kr.hhplus.be.domain.coupon.DiscountType
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.presentation.dto.request.CouponIssueRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestConstructor
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class CouponControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) : BehaviorSpec() {

    @MockkBean
    private lateinit var couponService: CouponService

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("쿠폰 발급 API") {
            When("유효한 사용자 ID와 쿠폰 ID로 발급을 요청하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val couponId = 1L
                val request = CouponIssueRequest(userId, couponId)
                val now = LocalDateTime.now()

                val mockUserCouponInfo = UserCouponInfo(
                    id = 1L,
                    userId = userId,
                    couponId = couponId,
                    couponName = "10% 할인 쿠폰",
                    discountType = DiscountType.PERCENTAGE,
                    discountValue = 10,
                    status = CouponStatus.AVAILABLE,
                    expiresAt = now.plusDays(30),
                    issuedAt = now,
                    usedAt = null
                )

                every { couponService.issue(any()) } returns mockUserCouponInfo

                val result = mockMvc.perform(
                    post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("201 상태코드와 발급된 쿠폰 정보를 반환한다") {
                    result.andExpect(status().isCreated)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.id").value(1))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.couponId").value(couponId))
                        .andExpect(jsonPath("$.data.couponName").value("10% 할인 쿠폰"))
                        .andExpect(jsonPath("$.data.discountType").value("PERCENTAGE"))
                        .andExpect(jsonPath("$.data.discountValue").value(10))
                        .andExpect(jsonPath("$.data.expiresAt").exists())
                        .andExpect(jsonPath("$.data.issuedAt").exists())
                    
                    verify(exactly = 1) { couponService.issue(any()) }
                }
            }

            When("이미 발급받은 쿠폰을 다시 발급 요청하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val couponId = 1L
                val request = CouponIssueRequest(userId, couponId)

                every { couponService.issue(any()) } throws BusinessException(ErrorCode.COUPON_ALREADY_ISSUED)

                val result = mockMvc.perform(
                    post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("409 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isConflict)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("COUPON_ALREADY_ISSUED"))
                    
                    verify(exactly = 1) { couponService.issue(any()) }
                }
            }

            When("존재하지 않는 쿠폰 ID로 발급을 요청하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val couponId = 999L
                val request = CouponIssueRequest(userId, couponId)

                every { couponService.issue(any()) } throws BusinessException(ErrorCode.COUPON_NOT_FOUND)

                val result = mockMvc.perform(
                    post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("COUPON_NOT_FOUND"))
                    
                    verify(exactly = 1) { couponService.issue(any()) }
                }
            }

            When("만료된 쿠폰을 발급 요청하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val couponId = 1L
                val request = CouponIssueRequest(userId, couponId)

                every { couponService.issue(any()) } throws BusinessException(ErrorCode.COUPON_EXPIRED)

                val result = mockMvc.perform(
                    post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("COUPON_EXPIRED"))
                    
                    verify(exactly = 1) { couponService.issue(any()) }
                }
            }

            When("쿠폰이 모두 소진되었을 때 발급을 요청하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val couponId = 1L
                val request = CouponIssueRequest(userId, couponId)

                every { couponService.issue(any()) } throws BusinessException(ErrorCode.COUPON_SOLD_OUT)

                val result = mockMvc.perform(
                    post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("COUPON_SOLD_OUT"))
                    
                    verify(exactly = 1) { couponService.issue(any()) }
                }
            }

            When("잘못된 JSON 형식으로 요청하면") {
                clearMocks(couponService)
                
                val invalidJson = "{ \"userId\": \"invalid\", \"couponId\": \"not_number\" }"

                val result = mockMvc.perform(
                    post("/api/v1/coupons/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }
        }

        Given("사용자 쿠폰 목록 조회 API") {
            When("유효한 사용자 ID로 쿠폰 목록을 조회하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val now = LocalDateTime.now()
                
                val mockUserCoupons = listOf(
                    UserCouponInfo(
                        id = 1L,
                        userId = userId,
                        couponId = 1L,
                        couponName = "10% 할인 쿠폰",
                        discountType = DiscountType.PERCENTAGE,
                        discountValue = 10,
                        status = CouponStatus.AVAILABLE,
                        expiresAt = now.plusDays(30),
                        issuedAt = now.minusDays(1),
                        usedAt = null
                    ),
                    UserCouponInfo(
                        id = 2L,
                        userId = userId,
                        couponId = 2L,
                        couponName = "5000원 할인 쿠폰",
                        discountType = DiscountType.FIXED,
                        discountValue = 5000,
                        status = CouponStatus.USED,
                        expiresAt = now.plusDays(15),
                        issuedAt = now.minusDays(5),
                        usedAt = now.minusDays(2)
                    )
                )

                every { couponService.getUserCoupons(userId) } returns mockUserCoupons

                val result = mockMvc.perform(
                    get("/api/v1/coupons")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 사용자 쿠폰 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.coupons").isArray)
                        .andExpect(jsonPath("$.data.coupons.length()").value(2))
                        .andExpect(jsonPath("$.data.coupons[0].id").value(1))
                        .andExpect(jsonPath("$.data.coupons[0].couponName").value("10% 할인 쿠폰"))
                        .andExpect(jsonPath("$.data.coupons[0].status").value("AVAILABLE"))
                        .andExpect(jsonPath("$.data.coupons[1].id").value(2))
                        .andExpect(jsonPath("$.data.coupons[1].couponName").value("5000원 할인 쿠폰"))
                        .andExpect(jsonPath("$.data.coupons[1].status").value("USED"))
                    
                    verify(exactly = 1) { couponService.getUserCoupons(userId) }
                }
            }

            When("쿠폰이 없는 사용자 ID로 조회하면") {
                clearMocks(couponService)
                
                val userId = 2L
                val emptyList = emptyList<UserCouponInfo>()

                every { couponService.getUserCoupons(userId) } returns emptyList

                val result = mockMvc.perform(
                    get("/api/v1/coupons")
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 빈 쿠폰 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.coupons").isArray)
                        .andExpect(jsonPath("$.data.coupons.length()").value(0))
                    
                    verify(exactly = 1) { couponService.getUserCoupons(userId) }
                }
            }

            When("status 파라미터와 함께 쿠폰 목록을 조회하면") {
                clearMocks(couponService)
                
                val userId = 1L
                val status = "AVAILABLE"
                val now = LocalDateTime.now()
                
                val mockAvailableCoupons = listOf(
                    UserCouponInfo(
                        id = 1L,
                        userId = userId,
                        couponId = 1L,
                        couponName = "10% 할인 쿠폰",
                        discountType = DiscountType.PERCENTAGE,
                        discountValue = 10,
                        status = CouponStatus.AVAILABLE,
                        expiresAt = now.plusDays(30),
                        issuedAt = now.minusDays(1),
                        usedAt = null
                    )
                )

                every { couponService.getUserCoupons(userId) } returns mockAvailableCoupons

                val result = mockMvc.perform(
                    get("/api/v1/coupons")
                        .param("userId", userId.toString())
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 필터링된 쿠폰 목록을 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.coupons").isArray)
                        .andExpect(jsonPath("$.data.coupons.length()").value(1))
                        .andExpect(jsonPath("$.data.coupons[0].status").value("AVAILABLE"))
                    
                    verify(exactly = 1) { couponService.getUserCoupons(userId) }
                }
            }

            When("userId 파라미터 없이 쿠폰 목록을 조회하면") {
                clearMocks(couponService)
                
                val result = mockMvc.perform(
                    get("/api/v1/coupons")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("잘못된 형식의 userId로 쿠폰 목록을 조회하면") {
                clearMocks(couponService)
                
                val result = mockMvc.perform(
                    get("/api/v1/coupons")
                        .param("userId", "invalid_user_id")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }
        }
    }
}
