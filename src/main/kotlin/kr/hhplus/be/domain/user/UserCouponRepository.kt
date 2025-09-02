package kr.hhplus.be.domain.user

interface UserCouponRepository {
    fun save(userCoupon: UserCoupon): UserCoupon
    fun findUserCouponDetails(userId: Long): List<UserCouponDetail>
    fun existsByUserIdAndCouponId(userId: Long, couponId: Long): Boolean
    fun findByUserId(userId: Long): List<UserCoupon>
    fun findByUserIdAndCouponId(userId: Long, couponId: Long): UserCoupon?
    fun findByCouponId(couponId: Long): List<UserCoupon>
    fun findById(userCouponId: Long): UserCoupon?
}
