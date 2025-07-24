package kr.hhplus.be.domain.user

interface UserCouponRepository {
    fun save(userCoupon: UserCoupon): UserCoupon
    fun existsByUserIdAndCouponId(userId: Long, couponId: Long): Boolean
    fun findByUserId(userId: Long): List<UserCoupon>
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon?
}
