package kr.hhplus.be.domain.coupon

interface CouponRepository {
    fun findById(id: Long): Coupon?
    fun save(coupon: Coupon): Coupon
}
