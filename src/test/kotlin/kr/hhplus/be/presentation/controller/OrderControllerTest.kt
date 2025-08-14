package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
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

class OrderControllerTest : FunSpec({
    val orderService: OrderService = mockk()
    val orderController = OrderController(orderService)
    val mockMvc: MockMvc = MockMvcBuilders.standaloneSetup(orderController)
        .setControllerAdvice(GlobalExceptionHandler())
        .build()
    val objectMapper = ObjectMapper().apply {
        registerModule(JavaTimeModule())
    }

    beforeTest {
        clearAllMocks()
    }

    context("주문 생성 API 테스트") {

        test("유효한 주문 생성 요청을 보내면 201 상태코드와 함께 주문이 생성된다") {
            // Given
            val userId = 1L
            val productId = 1L
            val quantity = 2
            val couponId = 1L

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
                orderNumber = "테스트",
                userId = userId,
                userCouponId = couponId,
                originalAmount = 20000,
                discountAmount = 1000,
                finalAmount = 19000,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = productId,
                        productName = "테스트 상품",
                        quantity = quantity,
                        price = 10000
                    )
                ),
                status = OrderStatus.PENDING
            )

            every { orderService.processOrder(any()) } returns orderInfo

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest))
            )

            // Then
            result.andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.originalAmount").value(20000))
                .andExpect(jsonPath("$.data.finalAmount").value(19000))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items[0].productName").value("테스트 상품"))

            verify(exactly = 1) { orderService.processOrder(any()) }
        }

        test("쿠폰 없이 주문 생성 요청을 보내면 정상적으로 처리된다") {
            // Given
            val userId = 1L
            val productId = 1L
            val quantity = 1

            val orderRequest = OrderRequest(
                userId = userId,
                items = listOf(
                    OrderItemRequest(
                        productId = productId,
                        quantity = quantity
                    )
                ),
                couponId = null
            )

            val orderInfo = OrderDto.OrderDetails(
                id = 1L,
                orderNumber = "테스트",
                userId = userId,
                userCouponId = null,
                originalAmount = 10000,
                discountAmount = 0,
                finalAmount = 10000,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = productId,
                        productName = "쿠폰 미사용 상품",
                        quantity = quantity,
                        price = 10000
                    )
                ),
                status = OrderStatus.PENDING
            )

            every { orderService.processOrder(any()) } returns orderInfo

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest))
            )

            // Then
            result.andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.discountAmount").value(0))
                .andExpect(jsonPath("$.data.finalAmount").value(10000))

            verify(exactly = 1) { orderService.processOrder(any()) }
        }

        test("복수 상품으로 주문 생성 요청을 보내면 정상적으로 처리된다") {
            // Given
            val userId = 1L
            val orderRequest = OrderRequest(
                userId = userId,
                items = listOf(
                    OrderItemRequest(productId = 1L, quantity = 2),
                    OrderItemRequest(productId = 2L, quantity = 1),
                    OrderItemRequest(productId = 3L, quantity = 3)
                ),
                couponId = null
            )

            val orderInfo = OrderDto.OrderDetails(
                id = 1L,
                orderNumber = "테스트",
                userId = userId,
                userCouponId = null,
                originalAmount = 60000,
                discountAmount = 0,
                finalAmount = 60000,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = 1L,
                        productName = "상품1",
                        quantity = 2,
                        price = 10000
                    ),
                    OrderDto.OrderItemDetails(
                        productId = 2L,
                        productName = "상품2",
                        quantity = 1,
                        price = 15000
                    ),
                    OrderDto.OrderItemDetails(
                        productId = 3L,
                        productName = "상품3",
                        quantity = 3,
                        price = 5000
                    )
                ),
                status = OrderStatus.PENDING
            )

            every { orderService.processOrder(any()) } returns orderInfo

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest))
            )

            // Then
            result.andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items.length()").value(3))
                .andExpect(jsonPath("$.data.originalAmount").value(60000))

            verify(exactly = 1) { orderService.processOrder(any()) }
        }

        test("잘못된 사용자 ID로 주문 생성을 요청하면 400 상태코드가 반환된다") {
            // Given
            val invalidRequest = OrderRequest(
                userId = 0L,
                items = listOf(
                    OrderItemRequest(productId = 1L, quantity = 1)
                ),
                couponId = null
            )

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )

            // Then
            result.andExpect(status().isBadRequest)
            verify(exactly = 0) { orderService.processOrder(any()) }
        }

        test("빈 상품 목록으로 주문 생성을 요청하면 400 상태코드가 반환된다") {
            // Given
            val invalidRequest = OrderRequest(
                userId = 1L,
                items = emptyList(),
                couponId = null
            )

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )

            // Then
            result.andExpect(status().isBadRequest)
            verify(exactly = 0) { orderService.processOrder(any()) }
        }

        test("잘못된 상품 ID로 주문 생성을 요청하면 400 상태코드가 반환된다") {
            // Given
            val invalidRequest = OrderRequest(
                userId = 1L,
                items = listOf(
                    OrderItemRequest(productId = 0L, quantity = 1)
                ),
                couponId = null
            )

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )

            // Then
            result.andExpect(status().isBadRequest)
            verify(exactly = 0) { orderService.processOrder(any()) }
        }

        test("잘못된 수량으로 주문 생성을 요청하면 400 상태코드가 반환된다") {
            // Given
            val invalidRequest = OrderRequest(
                userId = 1L,
                items = listOf(
                    OrderItemRequest(productId = 1L, quantity = 0)
                ),
                couponId = null
            )

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest))
            )

            // Then
            result.andExpect(status().isBadRequest)
            verify(exactly = 0) { orderService.processOrder(any()) }
        }
    }

    context("결제 처리 API 테스트") {

        test("유효한 결제 요청을 보내면 200 상태코드와 함께 결제가 처리된다") {
            // Given
            val orderId = 1L
            val userId = 1L

            val paymentRequest = PaymentRequest(
                userId = userId,
                orderId = orderId
            )

            val completedOrderInfo = OrderDto.OrderDetails(
                id = orderId,
                orderNumber = "테스트",
                userId = userId,
                userCouponId = null,
                originalAmount = 20000,
                discountAmount = 0,
                finalAmount = 20000,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = 1L,
                        productName = "결제 완료 상품",
                        quantity = 2,
                        price = 10000
                    )
                ),
                status = OrderStatus.COMPLETED
            )

            every { orderService.processPayment(any()) } returns completedOrderInfo

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders/$orderId/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            )

            // Then
            result.andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.finalAmount").value(20000))

            verify(exactly = 1) { orderService.processPayment(any()) }
        }

        test("잘못된 주문 ID로 결제 요청을 보내면 에러가 발생한다") {
            // Given
            val orderId = 999L
            val userId = 1L

            val paymentRequest = PaymentRequest(
                userId = userId,
                orderId = orderId
            )

            every { orderService.processPayment(any()) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders/$orderId/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            )

            // Then
            result.andExpect(status().isBadRequest)

            verify(exactly = 1) { orderService.processPayment(any()) }
        }

        test("잔액 부족으로 결제가 실패하면 적절한 에러 응답이 반환된다") {
            // Given
            val orderId = 1L
            val userId = 1L

            val paymentRequest = PaymentRequest(
                userId = userId,
                orderId = orderId
            )

            every { orderService.processPayment(any()) } throws BusinessException(ErrorCode.INSUFFICIENT_BALANCE)

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders/$orderId/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            )

            // Then
            result.andExpect(status().isPaymentRequired)

            verify(exactly = 1) { orderService.processPayment(any()) }
        }

        test("이미 처리된 주문으로 결제 요청을 보내면 적절한 에러 응답이 반환된다") {
            // Given
            val orderId = 1L
            val userId = 1L

            val paymentRequest = PaymentRequest(
                userId = userId,
                orderId = orderId
            )

            every { orderService.processPayment(any()) } throws BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders/$orderId/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            )

            // Then
            result.andExpect(status().isBadRequest)

            verify(exactly = 1) { orderService.processPayment(any()) }
        }
    }

    context("주문 조회 API 테스트") {

        test("유효한 주문 조회 요청을 보내면 200 상태코드와 함께 주문 정보가 반환된다") {
            // Given
            val orderId = 1L
            val userId = 1L

            val orderInfo = OrderDto.OrderDetails(
                id = orderId,
                orderNumber = "테스트",
                userId = userId,
                userCouponId = 1L,
                originalAmount = 25000,
                discountAmount = 2500,
                finalAmount = 22500,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = 1L,
                        productName = "조회된 상품1",
                        quantity = 1,
                        price = 15000
                    ),
                    OrderDto.OrderItemDetails(
                        productId = 2L,
                        productName = "조회된 상품2",
                        quantity = 1,
                        price = 10000
                    )
                ),
                status = OrderStatus.COMPLETED
            )

            every { orderService.getOrder(userId, orderId) } returns orderInfo

            // When
            val result = mockMvc.perform(
                get("/api/v1/orders/$orderId")
                    .param("userId", userId.toString())
            )

            // Then
            result.andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(orderId))
                .andExpect(jsonPath("$.data.userId").value(userId))
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.originalAmount").value(25000))
                .andExpect(jsonPath("$.data.discountAmount").value(2500))
                .andExpect(jsonPath("$.data.finalAmount").value(22500))
                .andExpect(jsonPath("$.data.items").isArray)
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].productName").value("조회된 상품1"))
                .andExpect(jsonPath("$.data.items[1].productName").value("조회된 상품2"))

            verify(exactly = 1) { orderService.getOrder(userId, orderId) }
        }

        test("존재하지 않는 주문을 조회하면 적절한 에러 응답이 반환된다") {
            // Given
            val orderId = 999L
            val userId = 1L

            every { orderService.getOrder(userId, orderId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            // When
            val result = mockMvc.perform(
                get("/api/v1/orders/$orderId")
                    .param("userId", userId.toString())
            )

            // Then
            result.andExpect(status().isBadRequest)

            verify(exactly = 1) { orderService.getOrder(userId, orderId) }
        }

        test("다른 사용자의 주문을 조회하면 적절한 에러 응답이 반환된다") {
            // Given
            val orderId = 1L
            val wrongUserId = 999L

            every { orderService.getOrder(wrongUserId, orderId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

            // When
            val result = mockMvc.perform(
                get("/api/v1/orders/$orderId")
                    .param("userId", wrongUserId.toString())
            )

            // Then
            result.andExpect(status().isBadRequest)

            verify(exactly = 1) { orderService.getOrder(wrongUserId, orderId) }
        }

        test("userId 파라미터 없이 주문 조회를 요청하면 400 상태코드가 반환된다") {
            // Given
            val orderId = 1L

            // When
            val result = mockMvc.perform(
                get("/api/v1/orders/$orderId")
            )

            // Then
            result.andExpect(status().isBadRequest)
            verify(exactly = 0) { orderService.getOrder(orderId) }
        }
    }

    context("주문 API 통합 시나리오 테스트") {

        test("주문 생성부터 결제까지의 전체 플로우가 정상적으로 동작한다") {
            // Given
            val userId = 1L
            val productId = 1L
            val quantity = 2

            val orderRequest = OrderRequest(
                userId = userId,
                items = listOf(
                    OrderItemRequest(productId = productId, quantity = quantity)
                ),
                couponId = null
            )

            val createdOrderInfo = OrderDto.OrderDetails(
                id = 1L,
                orderNumber = "테스트",
                userId = userId,
                userCouponId = null,
                originalAmount = 20000,
                discountAmount = 0,
                finalAmount = 20000,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = productId,
                        productName = "통합테스트 상품",
                        quantity = quantity,
                        price = 10000
                    )
                ),
                status = OrderStatus.PENDING
            )

            every { orderService.processOrder(any()) } returns createdOrderInfo

            // When
            val createResult = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest))
            )

            // Then
            createResult.andExpect(status().isCreated)
                .andExpect(jsonPath("$.data.status").value("PENDING"))

            // Given
            val orderId = 1L
            val paymentRequest = PaymentRequest(userId = userId, orderId = orderId)

            val completedOrderInfo = createdOrderInfo.copy(status = OrderStatus.COMPLETED)
            every { orderService.processPayment(any()) } returns completedOrderInfo

            // When
            val paymentResult = mockMvc.perform(
                post("/api/v1/orders/$orderId/pay")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(paymentRequest))
            )

            // Then
            paymentResult.andExpect(status().isOk)
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))

            verify(exactly = 1) { orderService.processOrder(any()) }
            verify(exactly = 1) { orderService.processPayment(any()) }
        }

        test("쿠폰을 사용한 주문 생성 시나리오가 정상적으로 동작한다") {
            // Given
            val userId = 1L
            val couponId = 1L

            val orderRequest = OrderRequest(
                userId = userId,
                items = listOf(
                    OrderItemRequest(productId = 1L, quantity = 1)
                ),
                couponId = couponId
            )

            val orderInfo = OrderDto.OrderDetails(
                id = 1L,
                orderNumber = "테스트",
                userId = userId,
                userCouponId = couponId,
                originalAmount = 50000,
                discountAmount = 5000,
                finalAmount = 45000,
                orderDate = LocalDateTime.now(),
                orderItems = listOf(
                    OrderDto.OrderItemDetails(
                        productId = 1L,
                        productName = "쿠폰 적용 상품",
                        quantity = 1,
                        price = 50000
                    )
                ),
                status = OrderStatus.PENDING
            )

            every { orderService.processOrder(any()) } returns orderInfo

            // When
            val result = mockMvc.perform(
                post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest))
            )

            // Then
            result.andExpect(status().isCreated)
                .andExpect(jsonPath("$.data.originalAmount").value(50000))
                .andExpect(jsonPath("$.data.discountAmount").value(5000))
                .andExpect(jsonPath("$.data.finalAmount").value(45000))

            verify(exactly = 1) { orderService.processOrder(any()) }
        }
    }
})
