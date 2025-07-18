package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.domain.enums.OrderStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.model.Order
import kr.hhplus.be.domain.model.OrderItem
import kr.hhplus.be.domain.model.Payment
import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.presentation.dto.request.OrderItemRequest
import kr.hhplus.be.presentation.dto.request.OrderRequest
import kr.hhplus.be.presentation.dto.request.PaymentRequest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(OrderController::class)
class OrderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockkBean
    private lateinit var orderService: OrderService

    @Test
    fun `주문 생성 API는 임시 주문 정보 반환`() {
        // given
        val orderItems = listOf(
            OrderItemRequest(productId = 1L, quantity = 2)
        )
        val request = OrderRequest(
            userId = 1L,
            items = orderItems,
            couponId = 1L
        )

        val mockOrder = Order(
            orderId = "test-order-123",
            userId = 1L,
            items = listOf(
                OrderItem(
                    productId = 1L,
                    productName = "아이폰",
                    price = 10000,
                    quantity = 2
                )
            ),
            originalAmount = 20000,
            discountAmount = 2000,
            finalAmount = 18000,
            status = OrderStatus.PENDING
        )

        every {
            orderService.createOrder(1L, any(), 1L)
        } returns mockOrder

        // when & then
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderId").value("test-order-123"))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.items[0].productId").value(1L))
            .andExpect(jsonPath("$.data.items[0].productName").value("아이폰"))
            .andExpect(jsonPath("$.data.items[0].price").value(10000))
            .andExpect(jsonPath("$.data.items[0].quantity").value(2))
            .andExpect(jsonPath("$.data.originalAmount").value(20000))
            .andExpect(jsonPath("$.data.discountAmount").value(2000))
            .andExpect(jsonPath("$.data.finalAmount").value(18000))
    }

    @Test
    fun `결제 처리 API는 결제 성공 응답 반환`() {
        // given
        val request = PaymentRequest(
            userId = 1L,
            paymentMethod = "BALANCE"
        )

        val mockPayment = Payment(
            orderId = 12345L,
            orderNumber = "order-54321",
            userId = 1L,
            finalAmount = 18000,
            status = OrderStatus.COMPLETED,
            orderedAt = LocalDateTime.now()
        )

        every { orderService.pay("test-order-123", 1L, "BALANCE") } returns mockPayment

        // when & then
        mockMvc.perform(
            post("/api/v1/orders/test-order-123/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderId").value(12345L))
            .andExpect(jsonPath("$.data.orderNumber").value("order-54321"))
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.finalAmount").value(18000))
            .andExpect(jsonPath("$.data.status").value("COMPLETED"))
    }

    @Test
    fun `잔액 부족 시 결제 실패 응답 반환`() {
        // given
        val request = PaymentRequest(
            userId = 999L,
            paymentMethod = "BALANCE"
        )

        every { orderService.pay("test-order-123", 999L, "BALANCE") } throws
                BusinessException(
                    ErrorCode.INSUFFICIENT_BALANCE
                )

        // when & then
        mockMvc.perform(
            post("/api/v1/orders/test-order-123/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isPaymentRequired)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_BALANCE"))
    }
}
