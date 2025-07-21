package kr.hhplus.be.application.service

import kr.hhplus.be.application.dto.OrderCreateCommand
import kr.hhplus.be.application.dto.OrderInfo
import kr.hhplus.be.application.dto.OrderItemInfo
import kr.hhplus.be.application.dto.PaymentProcessCommand
import kr.hhplus.be.application.port.`in`.OrderUseCase
import kr.hhplus.be.domain.enums.OrderStatus
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class OrderService : OrderUseCase {
    override fun createOrder(command: OrderCreateCommand): OrderInfo {
        if (command.items.isEmpty()) {
            throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)
        }
        // TODO: 상품 가격 조회, 재고 확인, 쿠폰 적용 등 로직 추가 예정
        val totalAmount = command.items.sumOf { it.quantity * 10000 }
        val orderItems = command.items.map { OrderItemInfo(it.productId, it.quantity, it.quantity * 10000) }

        return OrderInfo(
            id = 1L,
            userId = command.userId,
            orderStatus = OrderStatus.PENDING,
            totalAmount = totalAmount,
            orderedAt = LocalDateTime.now(),
            orderItems = orderItems
        )
    }

    override fun processPayment(command: PaymentProcessCommand): OrderInfo {
        if (command.userId == 999L) {
            throw BusinessException(ErrorCode.INSUFFICIENT_BALANCE)
        }
        // TODO: 결제 처리 및 주문 상태 업데이트 로직 추가 예정
        return OrderInfo(
            id = command.orderId,
            userId = command.userId,
            orderStatus = OrderStatus.COMPLETED,
            totalAmount = 18000,
            orderedAt = LocalDateTime.now(),
            orderItems = listOf(OrderItemInfo(1L, 1, 18000))
        )
    }

    override fun getOrder(orderId: Long): OrderInfo {
        // TODO: 추후 DB에서 주문 조회 로직 추가 예정
        return OrderInfo(
            id = orderId,
            userId = 1L,
            orderStatus = OrderStatus.COMPLETED,
            totalAmount = 18000,
            orderedAt = LocalDateTime.now(),
            orderItems = listOf(OrderItemInfo(1L, 1, 18000))
        )
    }
}
