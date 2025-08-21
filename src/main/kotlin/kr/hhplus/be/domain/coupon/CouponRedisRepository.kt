package kr.hhplus.be.domain.coupon

interface CouponRedisRepository {
    fun issueRequest(userId: Long, couponId: Long): String
}
