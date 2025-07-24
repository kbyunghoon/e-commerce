package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderCreateDto
import kr.hhplus.be.application.order.OrderDto.OrderInfo
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.OrderRepository
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository
) {

    fun createOrder(dto: OrderCreateDto): OrderInfo {
        val order = Order.create(
            userId = dto.userId,
            items = dto.items,
            originalAmount = dto.originalAmount,
            discountAmount = dto.discountAmount,
            finalAmount = dto.finalAmount,
            userCouponId = dto.couponId
        )

        val savedOrder = orderRepository.save(order)
        return OrderInfo.from(savedOrder)
    }

    fun completeOrder(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        order.completeOrder()
        val completedOrder = orderRepository.save(order)

        return OrderInfo.from(completedOrder)
    }

    fun cancelOrder(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        order.cancelOrder()
        val cancelledOrder = orderRepository.save(order)

        return OrderInfo.from(cancelledOrder)
    }

    fun getOrder(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        return OrderInfo.from(order)
    }

    fun getOrderForUpdate(orderId: Long): Order {
        return orderRepository.findByIdForUpdate(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
    }

    fun completePayment(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        order.completeOrder()
        val cancelledOrder = orderRepository.save(order)

        return OrderInfo.from(cancelledOrder)
    }
}
