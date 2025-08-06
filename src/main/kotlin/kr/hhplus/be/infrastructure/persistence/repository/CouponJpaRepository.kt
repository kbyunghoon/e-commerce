package kr.hhplus.be.infrastructure.persistence.repository

import jakarta.persistence.LockModeType
import kr.hhplus.be.infrastructure.entity.CouponEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CouponJpaRepository : JpaRepository<CouponEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") productId: Long): CouponEntity?
}
