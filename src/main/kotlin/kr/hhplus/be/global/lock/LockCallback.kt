package kr.hhplus.be.global.lock

fun interface LockCallback<T> {
    @Throws(Throwable::class)
    fun doInLock(): T
}