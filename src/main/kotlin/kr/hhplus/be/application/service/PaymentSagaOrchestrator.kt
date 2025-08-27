package kr.hhplus.be.application.service

import kr.hhplus.be.application.order.OrderDto
import kr.hhplus.be.application.order.PaymentProcessCommand
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
    private val balanceService: BalanceService,
    private val productService: ProductService,
    private val couponService: CouponService,
    private val orderService: OrderService,
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

        var currentSaga = sagaRepository.save(saga)

        try {
            currentSaga = executeStep(currentSaga, PaymentSagaStep.CREATE_ORDER)
            currentSaga = executeStep(currentSaga, PaymentSagaStep.DEDUCT_BALANCE)
            currentSaga = executeStep(currentSaga, PaymentSagaStep.DEDUCT_STOCK)
            currentSaga = executeStep(currentSaga, PaymentSagaStep.USE_COUPON)
            currentSaga = executeStep(currentSaga, PaymentSagaStep.COMPLETE_ORDER)

            currentSaga = currentSaga.updateStatus(SagaStatus.COMPLETED)
            sagaRepository.save(currentSaga)

            log.info("Payment Saga 완료 - sagaId: {}, orderId: {}", sagaId, command.orderId)
            return orderService.getOrder(command.orderId, command.userId)

        } catch (e: Exception) {
            log.error("Payment Saga 실패 - sagaId: {}, orderId: {}", sagaId, command.orderId, e)
            compensateSaga(currentSaga)
            throw e
        }
    }

    private fun executeStep(saga: PaymentSaga, step: PaymentSagaStep): PaymentSaga {
        log.debug("Saga 단계 실행 - sagaId: {}, step: {}", saga.sagaId, step)

        try {
            when (step) {
                PaymentSagaStep.CREATE_ORDER -> {}

                PaymentSagaStep.DEDUCT_BALANCE -> {
                    balanceService.deductBalanceForSaga(saga.userId, saga.finalAmount)
                }

                PaymentSagaStep.DEDUCT_STOCK -> {
                    orderService.deductStockForSaga(saga.orderId, saga.userId)
                }

                PaymentSagaStep.USE_COUPON -> {
                    saga.userCouponId?.let { couponId ->
                        couponService.use(saga.userId, couponId)
                    }
                }

                PaymentSagaStep.COMPLETE_ORDER -> {
                    orderService.completeOrderForSaga(saga.orderId)
                }
            }

            val updatedSaga = saga.markStepCompleted(step).updateStatus(SagaStatus.IN_PROGRESS)
            return sagaRepository.save(updatedSaga)

        } catch (e: Exception) {
            log.error("Saga 단계 실행 실패 - sagaId: {}, step: {}", saga.sagaId, step, e)
            throw e
        }
    }

    private fun compensateSaga(saga: PaymentSaga) {
        log.info("Saga 보상 트랜잭션 시작 - sagaId: {}", saga.sagaId)

        var currentSaga = saga.updateStatus(SagaStatus.COMPENSATING)
        sagaRepository.save(currentSaga)

        val stepsToCompensate = currentSaga.getStepsToCompensate()

        for (step in stepsToCompensate) {
            try {
                log.info("Saga 보상 단계 - sagaId: {}, step: {}", currentSaga.sagaId, step)
                compensateStep(currentSaga, step)
                currentSaga = currentSaga.markStepCompensated(step)
                sagaRepository.save(currentSaga)
            } catch (e: Exception) {
                log.error("Saga 보상 단계 실패 - sagaId: {}, step: {}", currentSaga.sagaId, step, e)
            }
        }

        currentSaga = currentSaga.updateStatus(SagaStatus.COMPENSATED)
        sagaRepository.save(currentSaga)

        log.info("Saga 보상 트랜잭션 완료 - sagaId: {}", saga.sagaId)
    }

    private fun compensateStep(saga: PaymentSaga, step: PaymentSagaStep) {
        log.debug("Saga 보상 단계 실행 - sagaId: {}, step: {}", saga.sagaId, step)

        when (step) {
            PaymentSagaStep.CREATE_ORDER -> {
                orderService.cancelOrderForSaga(saga.orderId)
            }

            PaymentSagaStep.DEDUCT_BALANCE -> {
                balanceService.refundBalanceForSaga(saga.userId, saga.finalAmount)
            }

            PaymentSagaStep.DEDUCT_STOCK -> {
                productService.restoreStockForSaga(saga.orderId)
            }

            PaymentSagaStep.USE_COUPON -> {
                saga.userCouponId?.let { couponId ->
                    couponService.restoreCouponForSaga(saga.userId, couponId)
                }
            }

            PaymentSagaStep.COMPLETE_ORDER -> {
                orderService.cancelOrderForSaga(saga.orderId)
            }
        }
    }
}