package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.order.OrderDto.OrderDetails
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import kr.hhplus.be.domain.order.events.OrderCompletedEvent
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
    fun processOrder(request: OrderCreateCommand): OrderDetails {
        val calculatedDetails = calculateOrderAmounts(request)

        val order = Order.create(
            userId = request.userId,
            originalAmount = calculatedDetails.totalAmount,
            discountAmount = calculatedDetails.discountAmount,
            finalAmount = calculatedDetails.finalAmount,
            userCouponId = request.userCouponId
        )

        val savedOrder = orderRepository.save(order)
        val orderItems = createOrderItems(request, calculatedDetails, savedOrder.id)
        val savedOrderItems = orderItemRepository.saveAll(orderItems)

        return OrderDetails.from(savedOrder, savedOrderItems)
    }

    @Transactional(readOnly = true)
    fun calculateOrderAmounts(request: OrderCreateCommand): OrderDto.CalculatedOrderDetails {
        val products = productService.validateOrderItems(request.items)
        val totalAmount = request.items.sumOf { orderItem ->
            val product = products.find { it.id == orderItem.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
            product.price * orderItem.quantity
        }

        val discountAmount = request.userCouponId?.let { couponId ->
            couponService.findAndValidateUserCoupon(request.userId, couponId)
            couponService.calculateDiscount(request.userId, couponId, totalAmount)
        } ?: 0

        val finalAmount = totalAmount - discountAmount
        return OrderDto.CalculatedOrderDetails(totalAmount, discountAmount, finalAmount, products)
    }

    @Transactional(readOnly = true)
    fun getOrder(orderId: Long, userId: Long): OrderDetails {
        val order =
            orderRepository.findByIdAndUserId(orderId, userId) ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        return OrderDetails.from(order, orderItems)
    }

    private fun createOrderItems(
        request: OrderCreateCommand,
        calculatedDetails: OrderDto.CalculatedOrderDetails,
        orderId: Long
    ): List<OrderItem> {
        return request.items.map { orderItemRequest ->
            val product = calculatedDetails.products.find { it.id == orderItemRequest.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            OrderItem(
                orderId = orderId,
                productId = orderItemRequest.productId,
                quantity = orderItemRequest.quantity,
                productName = product.name,
                pricePerItem = product.price,
                status = OrderStatus.PENDING
            )
        }
    }

    fun cancelOrder(orderId: Long): OrderDetails {
        val order = orderRepository.findByIdOrThrow(orderId)

        order.cancelOrder()
        val cancelledOrder = orderRepository.save(order)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.cancelOrder()
            item
        }

        val cancelledOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        return OrderDetails.from(cancelledOrder, cancelledOrderItems)
    }

    @Transactional
    fun completeOrderForSaga(orderId: Long) {
        val order = orderRepository.findByIdOrThrow(orderId)

        order.completeOrder()
        val completedOrder = orderRepository.save(order)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.completeOrder()
            item
        }

        val completedOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        val orderDetails = OrderDetails.from(completedOrder, completedOrderItems)

        publishOrderCompletedEvent(orderDetails)
    }

    @Transactional
    fun cancelOrderForSaga(orderId: Long) {
        val order = orderRepository.findByIdOrThrow(orderId)

        order.cancelOrder()
        orderRepository.save(order)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.cancelOrder()
            item
        }

        orderItemRepository.saveAll(updatedOrderItems)
    }

    private fun publishOrderCompletedEvent(orderDetails: OrderDetails) {
        val event = OrderCompletedEvent(
            orderId = orderDetails.id!!,
            userId = orderDetails.userId,
            totalAmount = orderDetails.originalAmount,
            finalAmount = orderDetails.finalAmount,
            discountAmount = orderDetails.discountAmount,
            userCouponId = orderDetails.userCouponId,
            orderItems = orderDetails.orderItems.map { item ->
                OrderCompletedEvent.OrderItemInfo(
                    productId = item.productId,
                    productName = item.productName,
                    quantity = item.quantity,
                    pricePerItem = item.price
                )
            }
        )

        applicationEventPublisher.publishEvent(event)
    }

    @Transactional(readOnly = true)
    fun getOrderForPayment(orderId: Long, userId: Long): OrderDetails {
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
        return OrderDetails.from(order, orderItems)
    }

    @Transactional
    fun deductStockForSaga(orderId: Long, userId: Long) {
        val order = getOrder(orderId, userId)

        order.orderItems.forEach { item ->
            productService.deductStock(item.productId, item.quantity)
        }
    }

    @Transactional
    fun restoreStockForSaga(orderId: Long, userId: Long) {
        val order = getOrder(orderId, userId)

        order.orderItems.forEach { item ->
            productService.restoreStock(item.productId, item.quantity)
        }
    }
}
