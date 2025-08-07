package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceRefundCommand
import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.order.OrderDto.OrderDetails
import kr.hhplus.be.application.order.PaymentOperationsStatus
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.product.ProductDto
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productService: ProductService,
    private val balanceService: BalanceService,
    private val couponService: CouponService
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


    @Transactional
    fun processPayment(request: PaymentProcessCommand): OrderDetails {
        val order = getOrderForPayment(request.orderId, request.userId)

        val paymentStatus = PaymentOperationsStatus()
        try {
            performPaymentOperations(
                userId = order.userId,
                finalAmount = order.finalAmount,
                userCouponId = order.userCouponId,
                items = order.orderItems,
                paymentStatus = paymentStatus
            )

            return completePayment(request.orderId)
        } catch (e: Exception) {
            rollbackPaymentOperations(
                userId = order.userId,
                finalAmount = order.finalAmount,
                couponId = order.userCouponId,
                paymentStatus = paymentStatus
            )
            throw e
        }
    }

    @Transactional(readOnly = true)
    private fun calculateOrderAmounts(request: OrderCreateCommand): OrderDto.CalculatedOrderDetails {
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

    private fun performPaymentOperations(
        userId: Long,
        finalAmount: Int,
        userCouponId: Long?,
        items: List<OrderDto.OrderItemDetails>,
        paymentStatus: PaymentOperationsStatus
    ) {
        balanceService.use(BalanceDeductCommand(userId = userId, amount = finalAmount))
        paymentStatus.balanceDeducted = true

        val stockDeductions = items.map { item ->
            ProductDto.ProductStockDeduction(
                productId = item.productId,
                quantity = item.quantity
            )
        }

        productService.batchDeductStock(stockDeductions)
        
        paymentStatus.deductedProducts.addAll(items)

        userCouponId?.let { id ->
            couponService.use(userId, id)
            paymentStatus.couponUsed = true
        }
    }

    private fun rollbackPaymentOperations(
        userId: Long,
        finalAmount: Int,
        couponId: Long?,
        paymentStatus: PaymentOperationsStatus
    ) {
        if (paymentStatus.balanceDeducted) {
            balanceService.refund(BalanceRefundCommand(userId = userId, amount = finalAmount))
        }
        paymentStatus.deductedProducts.forEach { item ->
            productService.restoreStock(item.productId, item.quantity)
        }
        if (paymentStatus.couponUsed && couponId != null) {
            couponService.restore(userId, couponId)
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

    fun getOrder(orderId: Long): OrderDetails {
        val order = orderRepository.findByIdOrThrow(orderId)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        return OrderDetails.from(order, orderItems)
    }

    fun getDomainOrder(orderId: Long): Order {
        return orderRepository.findByIdOrThrow(orderId)
    }

    fun getOrderForPayment(orderId: Long, userId: Long): OrderDetails {
        val order = orderRepository.findByIdOrThrow(orderId)

        if (order.userId != userId) {
            throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
        }

        if (!order.isPending()) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)
        }

        val orderItems = orderItemRepository.findByOrderId(orderId)
        return OrderDetails.from(order, orderItems)
    }

    fun completePayment(orderId: Long): OrderDetails {
        val order = orderRepository.findByIdOrThrow(orderId)

        order.completeOrder()
        val completedOrder = orderRepository.save(order)

        val orderItems = orderItemRepository.findByOrderId(orderId)

        val updatedOrderItems = orderItems.map { item ->
            item.completeOrder()
            item
        }

        val completedOrderItems = orderItemRepository.saveAll(updatedOrderItems)

        return OrderDetails.from(completedOrder, completedOrderItems)
    }

    @Transactional(readOnly = true)
    fun getOrder(userId: Long, orderId: Long): OrderDetails {
        return getOrder(orderId)
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
}
