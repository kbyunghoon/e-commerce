package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import kr.hhplus.be.domain.order.OrderCompletedEvent
import kr.hhplus.be.domain.order.OrderItemInfo
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productService: ProductService,
    private val couponService: CouponService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    @Transactional
    fun processOrder(request: OrderCreateCommand): Order {
        val products = productService.validateOrderItems(request.items)
        val totalAmount = request.items.sumOf { orderItem ->
            val product = products.find { it.id == orderItem.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
            product.price * orderItem.quantity
        }

        val discountAmount = request.userCouponId?.let { couponId ->
            couponService.getUserCouponWithValid(request.userId, couponId)
            couponService.calculateDiscount(request.userId, couponId, totalAmount)
        } ?: 0

        val finalAmount = totalAmount - discountAmount

        val order = Order.create(
            userId = request.userId,
            originalAmount = totalAmount,
            discountAmount = discountAmount,
            finalAmount = finalAmount,
            userCouponId = request.userCouponId
        )

        val savedOrder = orderRepository.save(order)

        val orderItems = request.items.map { orderItemRequest ->
            val product = products.find { it.id == orderItemRequest.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            OrderItem(
                orderId = order.id,
                productId = orderItemRequest.productId,
                quantity = orderItemRequest.quantity,
                productName = product.name,
                pricePerItem = product.price,
                status = OrderStatus.PENDING
            )
        }

        val savedOrderItems = orderItemRepository.saveAll(orderItems)

        return savedOrder.withOrderItems(savedOrderItems)
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: Long, userId: Long): Order {
        val order =
            orderRepository.findByIdAndUserId(orderId, userId) ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        return order.withOrderItems(orderItems)
    }

    fun cancelOrder(orderId: Long): Order {
        val order = orderRepository.findByIdOrThrow(orderId)

        val updatedOrder = orderRepository.save(order.cancelOrder())

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.cancelOrder()
            item
        }

        val cancelledOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        return updatedOrder.withOrderItems(cancelledOrderItems)
    }

    @Transactional
    fun completeOrderForSaga(orderId: Long) {
        val order = orderRepository.findByIdOrThrow(orderId)

        val completedOrder = orderRepository.save(order.completeOrder())

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.completeOrder()
            item
        }

        val completedOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        publishOrderCompletedEvent(completedOrder.withOrderItems(completedOrderItems))
    }

    @Transactional
    fun cancelOrderForSaga(orderId: Long) {
        val order = orderRepository.findByIdOrThrow(orderId)

        orderRepository.save(order.cancelOrder())

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.cancelOrder()
            item
        }

        orderItemRepository.saveAll(updatedOrderItems)
    }

    private fun publishOrderCompletedEvent(orderDetails: Order) {
        val event = OrderCompletedEvent(
            orderId = orderDetails.id!!,
            userId = orderDetails.userId,
            totalAmount = orderDetails.originalAmount,
            finalAmount = orderDetails.finalAmount,
            discountAmount = orderDetails.discountAmount,
            userCouponId = orderDetails.userCouponId,
            orderItems = orderDetails.orderItems!!.map { item ->
                OrderItemInfo(
                    productId = item.productId,
                    productName = item.productName,
                    quantity = item.quantity,
                    pricePerItem = item.pricePerItem
                )
            }
        )

        applicationEventPublisher.publishEvent(event)
    }

    @Transactional(readOnly = true)
    fun getOrderForPayment(orderId: Long, userId: Long): Order {
        val order = orderRepository.findByIdOrThrow(orderId)

        if (order.userId != userId) {
            throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
        }

        if (order.isCompleted()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_COMPLETED)
        }

        if (order.isCancelled()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED)
        }

        val orderItems = orderItemRepository.findByOrderId(orderId)
        return order.withOrderItems(orderItems)
    }

    @Transactional
    fun deductStockForSaga(orderId: Long, userId: Long) {
        val order = getOrder(orderId, userId)

        order.orderItems?.forEach { item ->
            productService.deductStock(item.productId, item.quantity)
        }
    }

    @Transactional
    fun restoreStockForSaga(orderId: Long, userId: Long) {
        val order = getOrder(orderId, userId)

        order.orderItems?.forEach { item ->
            productService.restoreStock(item.productId, item.quantity)
        }
    }
}
