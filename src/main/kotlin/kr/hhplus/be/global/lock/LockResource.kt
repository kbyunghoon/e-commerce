package kr.hhplus.be.global.lock

enum class LockResource {
    COUPON,
    PRODUCT_STOCK,
    PRODUCT_BATCH_STOCK,
    USER_BALANCE,
    ORDER_PAYMENT;

    fun createKey(value: String): String {
        return "${name.lowercase()}:$value"
    }
}