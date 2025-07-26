package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderDto.OrderCreateDto
import kr.hhplus.be.application.order.OrderDto.OrderInfo
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import org.springframework.stereotype.Service

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository
) {

    fun createOrder(dto: OrderCreateDto): OrderInfo {
        val order = Order.create(
            userId = dto.userId,
            originalAmount = dto.originalAmount,
            discountAmount = dto.discountAmount,
            finalAmount = dto.finalAmount,
            userCouponId = dto.couponId
        )

        val savedOrder = orderRepository.save(order)

        val orderItems = dto.items.map { itemDto ->
            OrderItem.create(
                productId = itemDto.productId,
                quantity = itemDto.quantity,
                pricePerItem = itemDto.pricePerItem,
                orderId = savedOrder.id!!
            )
        }

        val savedOrderItems = orderItemRepository.saveAll(orderItems)

        return OrderInfo.from(savedOrder, savedOrderItems)
    }

    fun cancelOrder(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        order.cancelOrder()
        val cancelledOrder = orderRepository.save(order)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.cancelOrder()
            item
        }

        val cancelledOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        return OrderInfo.from(cancelledOrder, cancelledOrderItems)
    }

    fun getOrder(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        return OrderInfo.from(order, orderItems)
    }
    
    fun getDomainOrder(orderId: Long): Order {
        return orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
    }
    
    fun getOrderForPayment(orderId: Long, userId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        if (order.userId != userId) {
            throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
        }

        if (!order.isPending()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)
        }

        val orderItems = orderItemRepository.findByOrderId(orderId)
        return OrderInfo.from(order, orderItems)
    }

    fun completePayment(orderId: Long): OrderInfo {
        val order = orderRepository.findById(orderId)
            ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        order.completeOrder()
        val completedOrder = orderRepository.save(order)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.completeOrder()
            item
        }

        val completedOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        return OrderInfo.from(completedOrder, completedOrderItems)
    }
}
