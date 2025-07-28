package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.domain.order.OrderStatus
import kr.hhplus.be.presentation.dto.request.OrderItemRequest
import kr.hhplus.be.presentation.dto.request.OrderRequest
import kr.hhplus.be.presentation.dto.request.PaymentRequest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDateTime

class OrderControllerTest : BehaviorSpec({
    val orderService: OrderService = mockk()
    val orderController = OrderController(orderService)
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(orderController).build()
    val objectMapper = ObjectMapper()

    afterContainer {
        clearAllMocks()
    }

    Given("주문 생성 API 테스트") {
        val userId = 1L
        val productId = 1L
        val quantity = 2
        val couponId = 1L

        When("유효한 주문 생성 요청을 보내면") {
            val orderRequest = OrderRequest(
                userId = userId,
                items = listOf(
                    OrderItemRequest(
                        productId = productId,
                        quantity = quantity
                    )
                ),
                couponId = couponId
            )

            val orderInfo = OrderDto.OrderDetails(
                id = 1L,
                userId = userId,
                userCouponId = couponId,
                originalAmount = 20000,
                discountAmount = 1000,
                finalAmount = 19000,
                orderedAt = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = productId,
                        quantity = quantity,
                        price = 10000
                    )
                ),
                status = OrderStatus.PENDING
            )

            every { orderService.processOrder(any()) } returns orderInfo

            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest))
            )

            Then("주문이 성공적으로 생성되고 201 상태코드가 반환된다") {
                result.andExpect(status().isCreated)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.originalAmount").value(20000))
                    .andExpect(jsonPath("$.data.finalAmount").value(19000))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))

                verify(exactly = 1) { orderService.processOrder(any()) }
            }
        }

        When("잘못된 요청 데이터로 주문 생성을 요청하면") {
            val invalidRequest = OrderRequest(
                userId = 0L, // 잘못된 userId
                items = emptyList(), // 빈 상품 목록
                couponId = null
            )

            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )

            Then("400 상태코드가 반환된다") {
                result.andExpect(status().isBadRequest)
                verify(exactly = 0) { orderService.processOrder(any()) }
            }
        }
    }

    Given("결제 처리 API 테스트") {
        val orderId = 1L
        val userId = 1L

        When("유효한 결제 요청을 보내면") {
            val paymentRequest = PaymentRequest(
                orderId = orderId,
                userId = userId
            )

            val completedOrderInfo = OrderDto.OrderDetails(
                id = orderId,
                userId = userId,
                userCouponId = null,
                originalAmount = 20000,
                discountAmount = 0,
                finalAmount = 20000,
                orderedAt = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = 1L,
                        quantity = 2,
                        price = 10000
                    )
                ),
                status = OrderStatus.COMPLETED
            )

            every { orderService.processPayment(any()) } returns completedOrderInfo

            val result = mockMvc.perform(
                post("/api/v1/orders/$orderId/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            )

            Then("결제가 성공적으로 처리되고 200 상태코드가 반환된다") {
                result.andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.finalAmount").value(20000))

                verify(exactly = 1) { orderService.processPayment(any()) }
            }
        }
    }

    Given("주문 조회 API 테스트") {
        val orderId = 1L
        val userId = 1L

        When("유효한 주문 조회 요청을 보내면") {
            val orderInfo = OrderDto.OrderDetails(
                id = orderId,
                userId = userId,
                userCouponId = null,
                originalAmount = 20000,
                discountAmount = 0,
                finalAmount = 20000,
                orderedAt = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = 1L,
                        quantity = 2,
                        price = 10000
                    )
                ),
                status = OrderStatus.COMPLETED
            )

            every { orderService.getOrder(userId, orderId) } returns orderInfo

            val result = mockMvc.perform(
                get("/api/v1/orders/$orderId")
                    .param("userId", userId.toString())
            )

            Then("주문 정보가 성공적으로 반환되고 200 상태코드가 반환된다") {
                result.andExpect(status().isOk)
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.orderId").value(orderId))
                    .andExpect(jsonPath("$.data.userId").value(userId))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))

                verify(exactly = 1) { orderService.getOrder(userId, orderId) }
            }
        }
    }
})