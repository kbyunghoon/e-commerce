package kr.hhplus.be.application.service

import kr.hhplus.be.application.balance.BalanceDeductCommand
import kr.hhplus.be.application.balance.BalanceRefundCommand
import kr.hhplus.be.domain.order.saga.events.BalanceDeductedEvent
import kr.hhplus.be.domain.order.saga.events.BalanceDeductionFailedEvent
import kr.hhplus.be.domain.order.saga.events.BalanceDeductionRequestedEvent
import kr.hhplus.be.domain.order.saga.events.BalanceRefundRequestedEvent
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class BalanceSagaEventHandler(
    private val balanceService: BalanceService,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Async
    fun handleBalanceDeductionRequested(event: BalanceDeductionRequestedEvent) {
        try {
            log.info("잔액 차감 요청 - sagaId: {}, userId: {}, amount: {}", 
                event.sagaId, event.userId, event.amount)
            
            val command = BalanceDeductCommand(userId = event.userId, amount = event.amount)
            balanceService.use(command)
            
            applicationEventPublisher.publishEvent(
                BalanceDeductedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    userId = event.userId,
                    amount = event.amount
                )
            )
        } catch (e: Exception) {
            log.error("잔액 차감 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
            applicationEventPublisher.publishEvent(
                BalanceDeductionFailedEvent(
                    sagaId = event.sagaId,
                    orderId = event.orderId,
                    userId = event.userId,
                    amount = event.amount,
                    reason = e.message ?: "잔액 차감 실패"
                )
            )
        }
    }

    @EventListener
    @Async
    fun handleBalanceRefundRequested(event: BalanceRefundRequestedEvent) {
        try {
            log.info("잔액 환불 요청 - sagaId: {}, userId: {}, amount: {}", 
                event.sagaId, event.userId, event.amount)
            
            val command = BalanceRefundCommand(userId = event.userId, amount = event.amount)
            balanceService.refund(command)
        } catch (e: Exception) {
            log.error("잔액 환불 실패 - sagaId: {}, reason: {}", event.sagaId, e.message, e)
        }
    }
}