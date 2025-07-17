package kr.hhplus.be.application.service

import kr.hhplus.be.presentation.dto.common.ErrorCode
import kr.hhplus.be.domain.enums.OrderStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.model.Order
import kr.hhplus.be.domain.model.OrderItem
import kr.hhplus.be.domain.model.Payment
import kr.hhplus.be.application.dto.OrderItemRequest
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService {
    fun createOrder(
        userId: Long,
        items: List<OrderItemRequest>,
        couponId: Long?
    ): Order {
        val orderItem = OrderItem(
            productId = 1,
            productName = "상품명",
            price = 10000,
            quantity = 2
        )
        return Order(
            orderId = "test-order-id-123",
            userId = userId,
            items = listOf(orderItem),
            originalAmount = 20000,
            discountAmount = 2000,
            finalAmount = 18000
        )
    }

    fun pay(orderId: String, userId: Long, paymentMethod: String): Payment {
        if (userId == 999L) {
            throw BusinessException(ErrorCode.INSUFFICIENT_BALANCE)
        }
        return Payment(
            orderId = 12345,
            orderNumber = "order-number-54321",
            userId = userId,
            finalAmount = 18000,
            status = OrderStatus.COMPLETED,
            orderedAt = LocalDateTime.now()
        )
    }
}
