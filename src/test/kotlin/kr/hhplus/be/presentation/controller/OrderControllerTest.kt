package kr.hhplus.be.presentation.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.spring.SpringExtension
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.verify
import kr.hhplus.be.application.facade.OrderFacade
import kr.hhplus.be.application.order.OrderItemCreateCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.OrderStatus
import kr.hhplus.be.presentation.dto.request.OrderRequest
import kr.hhplus.be.presentation.dto.request.PaymentRequest
import kr.hhplus.be.presentation.dto.response.OrderItemResponse
import kr.hhplus.be.presentation.dto.response.OrderResponse
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
class OrderControllerTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val objectMapper: ObjectMapper
) : BehaviorSpec() {

    @MockkBean
    private lateinit var orderFacade: OrderFacade

    override fun extensions() = listOf(SpringExtension)

    init {
        Given("주문 생성 API") {
            When("유효한 주문 정보로 주문을 생성하면") {
                clearMocks(orderFacade)
                
                val userId = 1L
                val items = listOf(
                    OrderItemCreateCommand(productId = 1L, quantity = 2),
                    OrderItemCreateCommand(productId = 2L, quantity = 1)
                )
                val request = OrderRequest(
                    userId = userId,
                    items = items,
                    couponId = null
                )
                val now = LocalDateTime.now()

                val mockOrderResponse = OrderResponse(
                    orderId = 1L,
                    userId = userId,
                    items = listOf(
                        OrderItemResponse(
                            productId = 1L,
                            productName = "상품 1",
                            price = 10000,
                            quantity = 2,
                            totalPrice = 20000
                        ),
                        OrderItemResponse(
                            productId = 2L,
                            productName = "상품 2",
                            price = 15000,
                            quantity = 1,
                            totalPrice = 15000
                        )
                    ),
                    originalAmount = 35000,
                    discountAmount = 0,
                    finalAmount = 35000,
                    status = OrderStatus.PENDING,
                    orderedAt = now
                )

                every { orderFacade.processOrder(any()) } returns mockOrderResponse

                val result = mockMvc.perform(
                    post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("201 상태코드와 생성된 주문 정보를 반환한다") {
                    result.andExpect(status().isCreated)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.orderId").value(1))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.items").isArray)
                        .andExpect(jsonPath("$.data.items.length()").value(2))
                        .andExpect(jsonPath("$.data.originalAmount").value(35000))
                        .andExpect(jsonPath("$.data.finalAmount").value(35000))
                        .andExpect(jsonPath("$.data.status").value("PENDING"))
                        .andExpect(jsonPath("$.data.orderedAt").exists())
                    
                    verify(exactly = 1) { orderFacade.processOrder(any()) }
                }
            }

            When("쿠폰을 사용하여 주문을 생성하면") {
                clearMocks(orderFacade)
                
                val userId = 1L
                val couponId = 1L
                val items = listOf(
                    OrderItemCreateCommand(productId = 1L, quantity = 1)
                )
                val request = OrderRequest(
                    userId = userId,
                    items = items,
                    couponId = couponId
                )
                val now = LocalDateTime.now()

                val mockOrderResponse = OrderResponse(
                    orderId = 2L,
                    userId = userId,
                    items = listOf(
                        OrderItemResponse(
                            productId = 1L,
                            productName = "상품 1",
                            price = 10000,
                            quantity = 1,
                            totalPrice = 10000
                        )
                    ),
                    originalAmount = 10000,
                    discountAmount = 1000,
                    finalAmount = 9000,
                    status = OrderStatus.PENDING,
                    orderedAt = now
                )

                every { orderFacade.processOrder(any()) } returns mockOrderResponse

                val result = mockMvc.perform(
                    post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("201 상태코드와 할인이 적용된 주문 정보를 반환한다") {
                    result.andExpect(status().isCreated)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.orderId").value(2))
                        .andExpect(jsonPath("$.data.originalAmount").value(10000))
                        .andExpect(jsonPath("$.data.discountAmount").value(1000))
                        .andExpect(jsonPath("$.data.finalAmount").value(9000))
                        .andExpect(jsonPath("$.data.status").value("PENDING"))
                    
                    verify(exactly = 1) { orderFacade.processOrder(any()) }
                }
            }

            When("빈 주문 항목으로 주문을 생성하면") {
                clearMocks(orderFacade)
                
                val userId = 1L
                val request = OrderRequest(
                    userId = userId,
                    items = emptyList(),
                    couponId = null
                )

                every { orderFacade.processOrder(any()) } throws BusinessException(ErrorCode.ORDER_ITEMS_CANNOT_BE_EMPTY)

                val result = mockMvc.perform(
                    post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("ORDER_ITEMS_CANNOT_BE_EMPTY"))
                    
                    verify(exactly = 1) { orderFacade.processOrder(any()) }
                }
            }

            When("존재하지 않는 상품으로 주문을 생성하면") {
                clearMocks(orderFacade)
                
                val userId = 1L
                val items = listOf(
                    OrderItemCreateCommand(productId = 999L, quantity = 1)
                )
                val request = OrderRequest(
                    userId = userId,
                    items = items,
                    couponId = null
                )

                every { orderFacade.processOrder(any()) } throws BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

                val result = mockMvc.perform(
                    post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("404 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isNotFound)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("PRODUCT_NOT_FOUND"))
                    
                    verify(exactly = 1) { orderFacade.processOrder(any()) }
                }
            }

            When("재고가 부족한 상품으로 주문을 생성하면") {
                clearMocks(orderFacade)
                
                val userId = 1L
                val items = listOf(
                    OrderItemCreateCommand(productId = 1L, quantity = 100)
                )
                val request = OrderRequest(
                    userId = userId,
                    items = items,
                    couponId = null
                )

                every { orderFacade.processOrder(any()) } throws BusinessException(ErrorCode.INSUFFICIENT_STOCK)

                val result = mockMvc.perform(
                    post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_STOCK"))
                    
                    verify(exactly = 1) { orderFacade.processOrder(any()) }
                }
            }
        }

        Given("결제 처리 API") {
            When("유효한 주문 ID와 사용자 ID로 결제를 처리하면") {
                clearMocks(orderFacade)
                
                val orderId = "1"
                val userId = 1L
                val request = PaymentRequest(userId = userId, orderId = 1L)
                val now = LocalDateTime.now()

                val mockOrderResponse = OrderResponse(
                    orderId = 1L,
                    userId = userId,
                    items = listOf(
                        OrderItemResponse(
                            productId = 1L,
                            productName = "상품 1",
                            price = 10000,
                            quantity = 1,
                            totalPrice = 10000
                        )
                    ),
                    originalAmount = 10000,
                    discountAmount = 0,
                    finalAmount = 10000,
                    status = OrderStatus.COMPLETED,
                    orderedAt = now
                )

                every { orderFacade.processPayment(any()) } returns mockOrderResponse

                val result = mockMvc.perform(
                    post("/api/v1/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("200 상태코드와 결제 완료된 주문 정보를 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.orderId").value(1))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                        .andExpect(jsonPath("$.data.finalAmount").value(10000))
                    
                    verify(exactly = 1) { orderFacade.processPayment(any()) }
                }
            }

            When("존재하지 않는 주문 ID로 결제를 처리하면") {
                clearMocks(orderFacade)
                
                val orderId = "999"
                val userId = 1L
                val request = PaymentRequest(userId = userId, orderId = 999L)

                every { orderFacade.processPayment(any()) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

                val result = mockMvc.perform(
                    post("/api/v1/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("ORDER_NOT_FOUND"))
                    
                    verify(exactly = 1) { orderFacade.processPayment(any()) }
                }
            }

            When("이미 처리된 주문에 대해 결제를 시도하면") {
                clearMocks(orderFacade)
                
                val orderId = "1"
                val userId = 1L
                val request = PaymentRequest(userId = userId, orderId = 1L)

                every { orderFacade.processPayment(any()) } throws BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)

                val result = mockMvc.perform(
                    post("/api/v1/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("ORDER_ALREADY_PROCESSED"))
                    
                    verify(exactly = 1) { orderFacade.processPayment(any()) }
                }
            }

            When("잔액이 부족한 상태에서 결제를 시도하면") {
                clearMocks(orderFacade)
                
                val orderId = "1"
                val userId = 1L
                val request = PaymentRequest(userId = userId, orderId = 1L)

                every { orderFacade.processPayment(any()) } throws BusinessException(ErrorCode.INSUFFICIENT_BALANCE)

                val result = mockMvc.perform(
                    post("/api/v1/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )

                Then("402 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isPaymentRequired)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("INSUFFICIENT_BALANCE"))
                    
                    verify(exactly = 1) { orderFacade.processPayment(any()) }
                }
            }
        }

        Given("주문 조회 API") {
            When("유효한 주문 ID와 사용자 ID로 주문을 조회하면") {
                clearMocks(orderFacade)
                
                val orderId = 1L
                val userId = 1L
                val now = LocalDateTime.now()

                val mockOrderResponse = OrderResponse(
                    orderId = orderId,
                    userId = userId,
                    items = listOf(
                        OrderItemResponse(
                            productId = 1L,
                            productName = "상품 1",
                            price = 10000,
                            quantity = 1,
                            totalPrice = 10000
                        )
                    ),
                    originalAmount = 10000,
                    discountAmount = 1000,
                    finalAmount = 9000,
                    status = OrderStatus.COMPLETED,
                    orderedAt = now
                )

                every { orderFacade.getOrder(userId, orderId) } returns mockOrderResponse

                val result = mockMvc.perform(
                    get("/api/v1/orders/{orderId}", orderId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("200 상태코드와 주문 정보를 반환한다") {
                    result.andExpect(status().isOk)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(true))
                        .andExpect(jsonPath("$.data.orderId").value(orderId))
                        .andExpect(jsonPath("$.data.userId").value(userId))
                        .andExpect(jsonPath("$.data.originalAmount").value(10000))
                        .andExpect(jsonPath("$.data.discountAmount").value(1000))
                        .andExpect(jsonPath("$.data.finalAmount").value(9000))
                        .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    
                    verify(exactly = 1) { orderFacade.getOrder(userId, orderId) }
                }
            }

            When("존재하지 않는 주문 ID로 조회하면") {
                clearMocks(orderFacade)
                
                val orderId = 999L
                val userId = 1L

                every { orderFacade.getOrder(userId, orderId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

                val result = mockMvc.perform(
                    get("/api/v1/orders/{orderId}", orderId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("ORDER_NOT_FOUND"))
                    
                    verify(exactly = 1) { orderFacade.getOrder(userId, orderId) }
                }
            }

            When("다른 사용자의 주문을 조회하면") {
                clearMocks(orderFacade)
                
                val orderId = 1L
                val userId = 2L

                every { orderFacade.getOrder(userId, orderId) } throws BusinessException(ErrorCode.ORDER_NOT_FOUND)

                val result = mockMvc.perform(
                    get("/api/v1/orders/{orderId}", orderId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드와 에러 메시지를 반환한다") {
                    result.andExpect(status().isBadRequest)
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(jsonPath("$.success").value(false))
                        .andExpect(jsonPath("$.error.code").value("ORDER_NOT_FOUND"))
                    
                    verify(exactly = 1) { orderFacade.getOrder(userId, orderId) }
                }
            }

            When("userId 파라미터 없이 주문을 조회하면") {
                clearMocks(orderFacade)
                
                val orderId = 1L

                val result = mockMvc.perform(
                    get("/api/v1/orders/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("잘못된 형식의 userId로 주문을 조회하면") {
                clearMocks(orderFacade)
                
                val orderId = 1L

                val result = mockMvc.perform(
                    get("/api/v1/orders/{orderId}", orderId)
                        .param("userId", "invalid_user_id")
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("잘못된 형식의 orderId로 주문을 조회하면") {
                clearMocks(orderFacade)
                
                val invalidOrderId = "invalid_order_id"
                val userId = 1L

                val result = mockMvc.perform(
                    get("/api/v1/orders/{orderId}", invalidOrderId)
                        .param("userId", userId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }
        }

        Given("잘못된 JSON 요청") {
            When("잘못된 JSON 형식으로 주문 생성을 요청하면") {
                clearMocks(orderFacade)
                
                val invalidJson = "{ \"userId\": \"invalid\", \"items\": \"not_array\" }"

                val result = mockMvc.perform(
                    post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }

            When("잘못된 JSON 형식으로 결제 요청하면") {
                clearMocks(orderFacade)
                
                val orderId = "1"
                val invalidJson = "{ \"userId\": \"invalid\", \"orderId\": \"not_number\" }"

                val result = mockMvc.perform(
                    post("/api/v1/orders/{orderId}/pay", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                )

                Then("400 상태코드를 반환한다") {
                    result.andExpect(status().isBadRequest)
                }
            }
        }
    }
}
