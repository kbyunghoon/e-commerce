package kr.hhplus.be.support.concurrent

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ConcurrentTestExecutor {
    
    fun execute(threadCount: Int, taskCount: Int, task: () -> Unit): ConcurrentTestResult {
        val result = ConcurrentTestResult()
        val executor = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(taskCount)
        
        repeat(taskCount) {
            executor.submit {
                try {
                    task()
                    result.addSuccess()
                } catch (e: Exception) {
                    result.addFailure(e)
                } finally {
                    latch.countDown()
                }
            }
        }
        
        latch.await(30, TimeUnit.SECONDS)
        executor.shutdown()
        
        return result
    }
}
