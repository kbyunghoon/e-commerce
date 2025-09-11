package kr.hhplus.be.global.cache

import java.time.Duration

enum class CacheType(val cacheName: String, val ttl: Duration) {
    POPULAR_PRODUCTS(CacheNames.PRODUCT_RANKING, Duration.ofMinutes(5))
}

object CacheNames {
    const val PRODUCT_RANKING = "productRanking"
}