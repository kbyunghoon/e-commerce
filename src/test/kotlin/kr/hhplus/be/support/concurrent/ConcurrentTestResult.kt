package kr.hhplus.be.support.concurrent

import java.util.concurrent.atomic.AtomicInteger

data class ConcurrentTestResult(
    private val successCount: AtomicInteger = AtomicInteger(0),
    private val failureCount: AtomicInteger = AtomicInteger(0),
    private val exceptions: MutableList<Exception> = mutableListOf()
) {
    fun getSuccessCount(): AtomicInteger = successCount
    fun getFailureCount(): AtomicInteger = failureCount
    fun getExceptions(): List<Exception> = exceptions.toList()
    
    fun addSuccess() {
        successCount.incrementAndGet()
    }
    
    fun addFailure(exception: Exception) {
        failureCount.incrementAndGet()
        exceptions.add(exception)
    }
}
