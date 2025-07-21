package kr.hhplus.be.adapter.`in`.web.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import kr.hhplus.be.application.dto.OrderCreateCommand
import kr.hhplus.be.application.dto.OrderInfo
import kr.hhplus.be.application.dto.OrderItemCreateCommand
import kr.hhplus.be.application.dto.OrderItemInfo
import kr.hhplus.be.application.dto.PaymentProcessCommand
import kr.hhplus.be.application.port.`in`.OrderUseCase
import kr.hhplus.be.domain.enums.OrderStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.adapter.`in`.web.dto.request.OrderItemRequest
import kr.hhplus.be.adapter.`in`.web.dto.request.OrderRequest
import kr.hhplus.be.adapter.`in`.web.dto.request.PaymentRequest
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
    private lateinit var orderUseCase: OrderUseCase

    @Test
    fun `주문 생성 API는 임시 주문 정보 반환`() {
        // given
        val orderItemRequests = listOf(
            OrderItemRequest(productId = 1L, quantity = 2)
        )
        val request = OrderRequest(
            userId = 1L,
            items = orderItemRequests,
            couponId = 1L
        )

        val orderItemCreateCommands = listOf(
            OrderItemCreateCommand(productId = 1L, quantity = 2)
        )
        val createCommand = OrderCreateCommand(
            userId = 1L,
            items = orderItemCreateCommands,
            couponId = 1L
        )

        val mockOrderInfo = OrderInfo(
            id = 123L,
            userId = 1L,
            orderStatus = OrderStatus.PENDING,
            totalAmount = 20000,
            orderedAt = LocalDateTime.now(),
            orderItems = listOf(OrderItemInfo(productId = 1L, quantity = 2, price = 10000))
        )

        every { orderUseCase.createOrder(createCommand) } returns mockOrderInfo

        // when & then
        mockMvc.perform(
            post("/api/v1/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderId").value("123")) // Long to String
            .andExpect(jsonPath("$.data.userId").value(1L))
            .andExpect(jsonPath("$.data.status").value("PENDING"))
            .andExpect(jsonPath("$.data.items[0].productId").value(1L))
            .andExpect(jsonPath("$.data.items[0].productName").value("상품명")) // 임시값
            .andExpect(jsonPath("$.data.items[0].price").value(10000))
            .andExpect(jsonPath("$.data.items[0].quantity").value(2))
            .andExpect(jsonPath("$.data.originalAmount").value(20000))
            .andExpect(jsonPath("$.data.discountAmount").value(0)) // 임시값
            .andExpect(jsonPath("$.data.finalAmount").value(20000))
    }

    @Test
    fun `결제 처리 API는 결제 성공 응답 반환`() {
        // given
        val request = PaymentRequest(
            userId = 1L,
            paymentMethod = "BALANCE"
        )

        val processCommand = PaymentProcessCommand(
            orderId = 123L,
            userId = 1L
        )

        val mockOrderInfo = OrderInfo(
            id = 123L,
            userId = 1L,
            orderStatus = OrderStatus.COMPLETED,
            totalAmount = 18000,
            orderedAt = LocalDateTime.now(),
            orderItems = listOf(OrderItemInfo(productId = 1L, quantity = 1, price = 18000))
        )

        every { orderUseCase.processPayment(processCommand) } returns mockOrderInfo

        // when & then
        mockMvc.perform(
            post("/api/v1/orders/123/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.orderId").value(123L)) // Long
            .andExpect(jsonPath("$.data.orderNumber").value("123")) // Long to String
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

        val processCommand = PaymentProcessCommand(
            orderId = 123L,
            userId = 999L
        )

        every { orderUseCase.processPayment(processCommand) } throws
                BusinessException(
                    ErrorCode.INSUFFICIENT_BALANCE
                )

        // when & then
        mockMvc.perform(
            post("/api/v1/orders/123/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isPaymentRequired)
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_BALANCE"))
    }
}
