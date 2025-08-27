package kr.hhplus.be.domain.order.saga.events

import java.time.LocalDateTime

sealed class SagaEvent(
    open val sagaId: String,
    open val orderId: Long,
    open val timestamp: LocalDateTime = LocalDateTime.now()
)

data class PaymentSagaStartedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val finalAmount: Int,
    val userCouponId: Long?,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class BalanceDeductionRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val amount: Int,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class BalanceDeductedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val amount: Int,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class BalanceDeductionFailedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val amount: Int,
    val reason: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class StockDeductionRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class StockDeductedEvent(
    override val sagaId: String,
    override val orderId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class StockDeductionFailedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val reason: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class CouponUsageRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val couponId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class CouponUsedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val couponId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class CouponUsageFailedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val couponId: Long,
    val reason: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class OrderCompletionRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class OrderCompletedEvent(
    override val sagaId: String,
    override val orderId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class OrderCompletionFailedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val reason: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class PaymentSagaCompletedEvent(
    override val sagaId: String,
    override val orderId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class PaymentSagaFailedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val failedStep: String,
    val reason: String,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

// 보상 이벤트들
data class BalanceRefundRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val amount: Int,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class StockRestoreRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class CouponRestoreRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    val userId: Long,
    val couponId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)

data class OrderCancellationRequestedEvent(
    override val sagaId: String,
    override val orderId: Long,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : SagaEvent(sagaId, orderId, timestamp)