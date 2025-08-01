package kr.hhplus.be.application.facade

import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.order.OrderDto.*
import kr.hhplus.be.application.order.PaymentOperationsStatus
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderFacade(
    private val orderService: OrderService,
    private val productService: ProductService,
    private val balanceService: BalanceService,
    private val couponService: CouponService
) {

    @Transactional
    fun processOrder(request: OrderCreateCommand): OrderDto.OrderInfo {
        val calculatedDetails = calculateOrderAmounts(request)
        val orderCreateDTO = toOrderCreateDto(request, calculatedDetails)

        return orderService.createOrder(orderCreateDTO)
    }

    private fun toOrderCreateDto(request: OrderCreateCommand, details: CalculatedOrderDetails): OrderCreateDto {
        val orderItems = request.items.map { orderItemRequest ->
            val product = details.products.find { it.id == orderItemRequest.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)

            OrderItem(
                productId = orderItemRequest.productId,
                quantity = orderItemRequest.quantity,
                pricePerItem = product.price,
                status = OrderStatus.PENDING
            )
        }

        return OrderCreateDto(
            userId = request.userId,
            items = orderItems,
            originalAmount = details.totalAmount,
            discountAmount = details.discountAmount,
            finalAmount = details.finalAmount,
            couponId = request.couponId
        )
    }

    @Transactional
    fun processPayment(request: PaymentProcessCommand): OrderDto.OrderInfo {
        val order = orderService.getOrderForPayment(request.orderId, request.userId)

        val paymentStatus = PaymentOperationsStatus()
        try {
            performPaymentOperations(
                userId = order.userId,
                finalAmount = order.finalAmount,
                userCouponId = order.userCouponId,
                items = order.orderItems,
                paymentStatus = paymentStatus
            )

            return orderService.completePayment(request.orderId)
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

    private fun calculateOrderAmounts(request: OrderCreateCommand): CalculatedOrderDetails {
        val products = productService.validateOrderItems(request.items)
        val totalAmount = request.items.sumOf { orderItem ->
            val product = products.find { it.id == orderItem.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
            product.price * orderItem.quantity
        }

        val discountAmount = request.couponId?.let { couponId ->
            couponService.findAndValidateUserCoupon(request.userId, couponId)
            couponService.calculateDiscount(request.userId, couponId, totalAmount)
        } ?: 0

        val finalAmount = totalAmount - discountAmount
        return CalculatedOrderDetails(totalAmount, discountAmount, finalAmount, products)
    }

    private fun performPaymentOperations(
        userId: Long,
        finalAmount: Int,
        userCouponId: Long?,
        items: List<OrderDto.OrderItemInfo>,
        paymentStatus: PaymentOperationsStatus
    ) {
        balanceService.use(BalanceDeductCommand(userId = userId, amount = finalAmount))
        paymentStatus.balanceDeducted = true

        items.forEach { item ->
            productService.deductStock(item.productId, item.quantity)
            paymentStatus.deductedProducts.add(item)
        }

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
            balanceService.refund(userId, finalAmount)
        }
        paymentStatus.deductedProducts.forEach { item ->
            productService.restoreStock(item.productId, item.quantity)
        }
        if (paymentStatus.couponUsed && couponId != null) {
            couponService.restore(userId, couponId)
        }
    }

    @Transactional(readOnly = true)
    fun getOrder(userId: Long, orderId: Long): OrderDto.OrderInfo {
        return orderService.getOrder(orderId)
    }
}
