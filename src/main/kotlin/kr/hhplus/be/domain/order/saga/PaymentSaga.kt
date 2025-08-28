package kr.hhplus.be.domain.order.saga

import com.fasterxml.jackson.annotation.JsonIgnore
import java.time.LocalDateTime

data class PaymentSaga(
    val sagaId: String,
    val orderId: Long,
    val userId: Long,
    val finalAmount: Int,
    val userCouponId: Long?,
    val currentStep: PaymentSagaStep,
    val status: SagaStatus,
    val completedSteps: MutableList<PaymentSagaStep> = mutableListOf(),
    val compensatedSteps: MutableList<PaymentSagaStep> = mutableListOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun markStepCompleted(step: PaymentSagaStep): PaymentSaga {
        completedSteps.add(step)
        return this.copy(
            currentStep = getNextStep(step),
            updatedAt = LocalDateTime.now()
        )
    }

    fun markStepCompensated(step: PaymentSagaStep): PaymentSaga {
        compensatedSteps.add(step)
        return this.copy(
            updatedAt = LocalDateTime.now()
        )
    }

    fun updateStatus(newStatus: SagaStatus): PaymentSaga {
        return this.copy(
            status = newStatus,
            updatedAt = LocalDateTime.now()
        )
    }

    private fun getNextStep(currentStep: PaymentSagaStep): PaymentSagaStep {
        val steps = PaymentSagaStep.values()
        val currentIndex = steps.indexOf(currentStep)
        return if (currentIndex < steps.size - 1) {
            steps[currentIndex + 1]
        } else {
            currentStep
        }
    }

    @JsonIgnore
    fun getStepsToCompensate(): List<PaymentSagaStep> {
        return completedSteps.filter { !compensatedSteps.contains(it) }.reversed()
    }
}