package kr.hhplus.be.application.facade

import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.order.CalculatedOrderDetails
import kr.hhplus.be.application.order.OrderCreateCommand
import kr.hhplus.be.application.order.OrderCreateDto
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.service.BalanceService
import kr.hhplus.be.application.service.CouponService
import kr.hhplus.be.application.service.OrderService
import kr.hhplus.be.application.service.ProductService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.OrderItem
import kr.hhplus.be.domain.order.OrderStatus
import kr.hhplus.be.presentation.dto.response.OrderResponse
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
    fun processOrder(request: OrderCreateCommand): OrderResponse {
        val calculatedDetails = calculateOrderAmounts(request)
        val orderCreateDTO = toOrderCreateDto(request, calculatedDetails)

        val createdOrder = orderService.createOrder(orderCreateDTO)

        return OrderResponse.from(createdOrder)
    }

    private fun toOrderCreateDto(request: OrderCreateCommand, details: CalculatedOrderDetails): OrderCreateDto {
        val orderItems = request.items.map { orderItem ->
            val product = details.products.find { it.id == orderItem.productId }
                ?: throw BusinessException(ErrorCode.PRODUCT_NOT_FOUND)
            OrderItem(
                productId = orderItem.productId,
                productName = product.name,
                quantity = orderItem.quantity,
                price = product.price
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
    fun processPayment(request: PaymentProcessCommand): OrderResponse {
        val order = orderService.getOrderForUpdate(request.orderId)

        if (order.userId != request.userId) {
            throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
        }

        if (order.status != OrderStatus.PENDING) {
            throw BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED)
        }

        performPaymentOperations(
            userId = order.userId,
            finalAmount = order.finalAmount,
            couponId = order.userCouponId,
            items = order.items
        )

        val completedOrder = orderService.completePayment(request.orderId)

        return OrderResponse.from(completedOrder)
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

    private fun performPaymentOperations(userId: Long, finalAmount: Int, couponId: Long?, items: List<OrderItem>) {
        balanceService.use(BalanceDeductCommand(userId = userId, amount = finalAmount))

        items.forEach { item ->
            productService.deductStock(item.productId, item.quantity)
        }

        couponId?.let { id ->
            couponService.use(userId, id)
        }
    }

    @Transactional(readOnly = true)
    fun getOrder(userId: Long, orderId: Long): OrderResponse {
        val order = orderService.getOrder(orderId)

        return OrderResponse.from(order)
    }
}
