package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.PaymentProcessCommand
import kr.hhplus.be.application.service.saga.SagaCouponService
import kr.hhplus.be.application.service.saga.SagaOrderService
import kr.hhplus.be.application.service.saga.SagaProductService
import kr.hhplus.be.application.service.saga.SagaUserService
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.order.Order
import kr.hhplus.be.domain.order.saga.PaymentSaga
import kr.hhplus.be.domain.order.saga.PaymentSagaRepository
import kr.hhplus.be.domain.order.saga.PaymentSagaStep
import kr.hhplus.be.domain.order.saga.SagaStatus
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class PaymentSagaOrchestrator(
    private val sagaRepository: PaymentSagaRepository,
    private val sagaOrderService: SagaOrderService,
    private val sagaUserService: SagaUserService,
    private val sagaProductService: SagaProductService,
    private val sagaCouponService: SagaCouponService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun executePaymentSaga(command: PaymentProcessCommand): Order {
        val sagaId = UUID.randomUUID().toString()
        log.info("Payment Saga 시작 - sagaId: {}, orderId: {}", sagaId, command.orderId)

        val orderDetails = sagaOrderService.getOrderForPayment(command.orderId, command.userId)
        var saga = PaymentSaga.create(
            sagaId = sagaId,
            orderId = command.orderId,
            userId = command.userId,
            finalAmount = orderDetails.finalAmount,
            userCouponId = orderDetails.userCouponId
        )

        sagaRepository.save(saga)

        try {
            log.info("Step 1: 잔액 차감 시작 - sagaId: {}, amount: {}", sagaId, saga.finalAmount)
            sagaUserService.deductBalance(saga.userId, saga.finalAmount)
            saga = saga.markStepCompleted(PaymentSagaStep.DEDUCT_BALANCE)
            sagaRepository.save(saga)
            log.info("Step 1: 잔액 차감 완료 - sagaId: {}", sagaId)

            log.info("Step 2: 재고 차감 시작 - sagaId: {}, orderId: {}", sagaId, saga.orderId)
            sagaProductService.deductStock(saga.orderId, saga.userId)
            saga = saga.markStepCompleted(PaymentSagaStep.DEDUCT_STOCK)
            sagaRepository.save(saga)
            log.info("Step 2: 재고 차감 완료 - sagaId: {}", sagaId)

            if (saga.userCouponId != null) {
                log.info("Step 3: 쿠폰 사용 시작 - sagaId: {}, couponId: {}", sagaId, saga.userCouponId)
                sagaCouponService.useCoupon(saga.userId, saga.userCouponId)
                saga = saga.markStepCompleted(PaymentSagaStep.USE_COUPON)
                sagaRepository.save(saga)
                log.info("Step 3: 쿠폰 사용 완료 - sagaId: {}", sagaId)
            }

            log.info("Step 4: 주문 완료 처리 시작 - sagaId: {}, orderId: {}", sagaId, saga.orderId)
            sagaOrderService.completeOrder(saga.orderId)
            saga = saga.markStepCompleted(PaymentSagaStep.COMPLETE_ORDER)
                .updateStatus(SagaStatus.COMPLETED)
            sagaRepository.save(saga)
            log.info("Payment Saga 완료 - sagaId: {}", sagaId)

            return sagaOrderService.getOrder(command.orderId, command.userId)

        } catch (e: Exception) {
            log.error("Payment Saga 실패 - sagaId: {}, reason: {}", sagaId, e.message, e)
            saga = saga.updateStatus(SagaStatus.COMPENSATING)
            sagaRepository.save(saga)

            startCompensation(saga)

            saga = saga.updateStatus(SagaStatus.FAILED)
            sagaRepository.save(saga)

            throw BusinessException(ErrorCode.PAYMENT_PROCESSING_FAILED)
        }
    }

    private fun startCompensation(saga: PaymentSaga) {
        log.info("Saga 보상 트랜잭션 시작 - sagaId: {}", saga.sagaId)

        val stepsToCompensate = saga.getStepsToCompensate()

        for (step in stepsToCompensate) {
            try {
                executeCompensation(saga, step)
                log.info("보상 트랜잭션 완료 - sagaId: {}, step: {}", saga.sagaId, step)
            } catch (e: Exception) {
                log.error(
                    "보상 트랜잭션 실패 - sagaId: {}, step: {}, reason: {}",
                    saga.sagaId, step, e.message, e
                )
            }
        }
    }

    private fun executeCompensation(saga: PaymentSaga, step: PaymentSagaStep) {
        when (step) {
            PaymentSagaStep.DEDUCT_BALANCE -> {
                log.info("잔액 환불 실행 - sagaId: {}, amount: {}", saga.sagaId, saga.finalAmount)
                sagaUserService.refundBalance(saga.userId, saga.finalAmount)
            }

            PaymentSagaStep.DEDUCT_STOCK -> {
                log.info("재고 복구 실행 - sagaId: {}, orderId: {}", saga.sagaId, saga.orderId)
                sagaProductService.restoreStock(saga.orderId, saga.userId)
            }

            PaymentSagaStep.USE_COUPON -> {
                saga.userCouponId?.let { couponId ->
                    log.info("쿠폰 복구 실행 - sagaId: {}, couponId: {}", saga.sagaId, couponId)
                    sagaCouponService.restoreCoupon(saga.userId, couponId)
                }
            }

            PaymentSagaStep.COMPLETE_ORDER -> {
                log.info("주문 취소 실행 - sagaId: {}, orderId: {}", saga.sagaId, saga.orderId)
                sagaOrderService.cancelOrder(saga.orderId)
            }

            PaymentSagaStep.CREATE_ORDER -> {}
        }
    }

    fun getSagaStatus(sagaId: String): SagaStatus? {
        return sagaRepository.findBySagaId(sagaId)?.status
    }

    @Transactional(readOnly = true)
    fun getPaymentResult(orderId: Long, userId: Long): Order? {
        val saga = sagaRepository.findByOrderId(orderId) ?: return null
        return if (saga.status == SagaStatus.COMPLETED) {
            sagaOrderService.getOrder(orderId, userId)
        } else {
            null
        }
    }
}