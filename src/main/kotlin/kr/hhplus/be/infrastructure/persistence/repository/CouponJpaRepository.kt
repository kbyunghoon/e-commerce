package kr.hhplus.be.infrastructure.persistence.repository

import kr.hhplus.be.domain.coupon.Coupon
import kr.hhplus.be.infrastructure.entity.CouponEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CouponJpaRepository : JpaRepository<CouponEntity, Long> {
}
