package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.domain.order.saga.PaymentSaga
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import kr.hhplus.be.domain.order.saga.PaymentSagaStep
import kr.hhplus.be.domain.order.saga.SagaStatus
import kr.hhplus.be.domain.order.saga.events.*
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.*

@Service
class PaymentSagaOrchestrator(
    private val sagaRepository: PaymentSagaRepository,
    private val orderService: OrderService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun executePaymentSaga(command: PaymentProcessCommand): OrderDto.OrderDetails {
        val sagaId = UUID.randomUUID().toString()
        log.info("Payment Saga 시작 - sagaId: {}, orderId: {}", sagaId, command.orderId)

        val orderDetails = orderService.getOrderForPayment(command.orderId, command.userId)

        val saga = PaymentSaga(
            sagaId = sagaId,
            orderId = command.orderId,
            userId = command.userId,
            finalAmount = orderDetails.finalAmount,
            userCouponId = orderDetails.userCouponId,
            currentStep = PaymentSagaStep.CREATE_ORDER,
            status = SagaStatus.STARTED
        )

        sagaRepository.save(saga)

        applicationEventPublisher.publishEvent(
            PaymentSagaStartedEvent(
                sagaId = sagaId,
                orderId = command.orderId,
                userId = command.userId,
                finalAmount = orderDetails.finalAmount,
                userCouponId = orderDetails.userCouponId
            )
        )

        return orderDetails
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    fun handlePaymentSagaStarted(event: PaymentSagaStartedEvent) {
        log.info("Payment Saga 시작 이벤트 처리 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)

        applicationEventPublisher.publishEvent(
            BalanceDeductionRequestedEvent(
                sagaId = event.sagaId,
                orderId = event.orderId,
                userId = event.userId,
                amount = event.finalAmount
            )
        )
    }

    @EventListener
    fun handleBalanceDeducted(event: BalanceDeductedEvent) {
        log.info("잔액 차감 완료 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)

        val saga = sagaRepository.findBySagaId(event.sagaId)
            ?: throw IllegalStateException("Saga not found: ${event.sagaId}")

        val updatedSaga = saga.markStepCompleted(PaymentSagaStep.DEDUCT_BALANCE)
            .updateStatus(SagaStatus.IN_PROGRESS)
        sagaRepository.save(updatedSaga)

        applicationEventPublisher.publishEvent(
            StockDeductionRequestedEvent(
                sagaId = event.sagaId,
                orderId = event.orderId,
                userId = event.userId
            )
        )
    }

    @EventListener
    fun handleStockDeducted(event: StockDeductedEvent) {
        log.info("재고 차감 완료 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)

        val saga = sagaRepository.findBySagaId(event.sagaId)
            ?: throw IllegalStateException("Saga not found: ${event.sagaId}")

        val updatedSaga = saga.markStepCompleted(PaymentSagaStep.DEDUCT_STOCK)
            .updateStatus(SagaStatus.IN_PROGRESS)
        sagaRepository.save(updatedSaga)

        if (saga.userCouponId != null) {
            applicationEventPublisher.publishEvent(
                CouponUsageRequestedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    userId = saga.userId,
                    couponId = saga.userCouponId
                )
            )
        } else {
            applicationEventPublisher.publishEvent(
                OrderCompletionRequestedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId
                )
            )
        }
    }

    @EventListener
    fun handleCouponUsed(event: CouponUsedEvent) {
        log.info("쿠폰 사용 완료 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)

        val saga = sagaRepository.findBySagaId(event.sagaId)
            ?: throw IllegalStateException("Saga not found: ${event.sagaId}")

        val updatedSaga = saga.markStepCompleted(PaymentSagaStep.USE_COUPON)
            .updateStatus(SagaStatus.IN_PROGRESS)
        sagaRepository.save(updatedSaga)

        applicationEventPublisher.publishEvent(
            OrderCompletionRequestedEvent(
                sagaId = event.sagaId,
                orderId = event.orderId
            )
        )
    }

    @EventListener
    fun handleOrderCompleted(event: OrderCompletedEvent) {
        log.info("주문 완료 - sagaId: {}, orderId: {}", event.sagaId, event.orderId)

        val saga = sagaRepository.findBySagaId(event.sagaId)
            ?: throw IllegalStateException("Saga not found: ${event.sagaId}")

        val updatedSaga = saga.markStepCompleted(PaymentSagaStep.COMPLETE_ORDER)
            .updateStatus(SagaStatus.COMPLETED)
        sagaRepository.save(updatedSaga)

        applicationEventPublisher.publishEvent(
            PaymentSagaCompletedEvent(
                sagaId = event.sagaId,
                orderId = event.orderId
            )
        )
    }

    @EventListener
    fun handleBalanceDeductionFailed(event: BalanceDeductionFailedEvent) {
        log.error("잔액 차감 실패 - sagaId: {}, reason: {}", event.sagaId, event.reason)
        handleSagaStepFailed(event.sagaId, PaymentSagaStep.DEDUCT_BALANCE, event.reason)
    }

    @EventListener
    fun handleStockDeductionFailed(event: StockDeductionFailedEvent) {
        log.error("재고 차감 실패 - sagaId: {}, reason: {}", event.sagaId, event.reason)
        handleSagaStepFailed(event.sagaId, PaymentSagaStep.DEDUCT_STOCK, event.reason)
    }

    @EventListener
    fun handleCouponUsageFailed(event: CouponUsageFailedEvent) {
        log.error("쿠폰 사용 실패 - sagaId: {}, reason: {}", event.sagaId, event.reason)
        handleSagaStepFailed(event.sagaId, PaymentSagaStep.USE_COUPON, event.reason)
    }

    @EventListener
    fun handleOrderCompletionFailed(event: OrderCompletionFailedEvent) {
        log.error("주문 완료 실패 - sagaId: {}, reason: {}", event.sagaId, event.reason)
        handleSagaStepFailed(event.sagaId, PaymentSagaStep.COMPLETE_ORDER, event.reason)
    }

    private fun handleSagaStepFailed(sagaId: String, failedStep: PaymentSagaStep, reason: String) {
        val saga = sagaRepository.findBySagaId(sagaId)
            ?: throw IllegalStateException("Saga not found: $sagaId")

        val updatedSaga = saga.updateStatus(SagaStatus.COMPENSATING)
        sagaRepository.save(updatedSaga)

        applicationEventPublisher.publishEvent(
            PaymentSagaFailedEvent(
                sagaId = sagaId,
                orderId = saga.orderId,
                failedStep = failedStep.stepName,
                reason = reason
            )
        )

        startCompensation(updatedSaga)
    }

    private fun startCompensation(saga: PaymentSaga) {
        log.info("Saga 보상 트랜잭션 시작 - sagaId: {}", saga.sagaId)

        val stepsToCompensate = saga.getStepsToCompensate()

        for (step in stepsToCompensate) {
            publishCompensationEvent(saga, step)
        }
    }

    private fun publishCompensationEvent(saga: PaymentSaga, step: PaymentSagaStep) {
        when (step) {
            PaymentSagaStep.CREATE_ORDER -> {
                applicationEventPublisher.publishEvent(
                    OrderCancellationRequestedEvent(
                        sagaId = saga.sagaId,
                        orderId = saga.orderId
                    )
                )
            }

            PaymentSagaStep.DEDUCT_BALANCE -> {
                applicationEventPublisher.publishEvent(
                    BalanceRefundRequestedEvent(
                        sagaId = saga.sagaId,
                        orderId = saga.orderId,
                        userId = saga.userId,
                        amount = saga.finalAmount
                    )
                )
            }

            PaymentSagaStep.DEDUCT_STOCK -> {
                applicationEventPublisher.publishEvent(
                    StockRestoreRequestedEvent(
                        sagaId = saga.sagaId,
                        orderId = saga.orderId,
                        userId = saga.userId,
                    )
                )
            }

            PaymentSagaStep.USE_COUPON -> {
                saga.userCouponId?.let { couponId ->
                    applicationEventPublisher.publishEvent(
                        CouponRestoreRequestedEvent(
                            sagaId = saga.sagaId,
                            orderId = saga.orderId,
                            userId = saga.userId,
                            couponId = couponId
                        )
                    )
                }
            }

            PaymentSagaStep.COMPLETE_ORDER -> {}
        }
    }

    fun getSagaStatus(sagaId: String): SagaStatus? {
        return sagaRepository.findBySagaId(sagaId)?.status
    }

    @Transactional(readOnly = true)
    fun getPaymentResult(orderId: Long, userId: Long): OrderDto.OrderDetails? {
        val saga = sagaRepository.findByOrderId(orderId) ?: return null
        return if (saga.status == SagaStatus.COMPLETED) {
            orderService.getOrder(orderId, userId)
        } else {
            null
        }
    }
}