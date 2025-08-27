package kr.hhplus.be.domain.order.saga

enum class PaymentSagaStep(val stepName: String) {
    CREATE_ORDER("CREATE_ORDER"),
    DEDUCT_BALANCE("DEDUCT_BALANCE"),
    DEDUCT_STOCK("DEDUCT_STOCK"),
    USE_COUPON("USE_COUPON"),
    COMPLETE_ORDER("COMPLETE_ORDER");

    companion object {
        fun getCompensationOrder(): List<PaymentSagaStep> {
            return listOf(
                COMPLETE_ORDER,
                USE_COUPON,
                DEDUCT_STOCK,
                DEDUCT_BALANCE,
                CREATE_ORDER
            )
        }
    }
}

enum class SagaStatus {
    STARTED,
    IN_PROGRESS,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}