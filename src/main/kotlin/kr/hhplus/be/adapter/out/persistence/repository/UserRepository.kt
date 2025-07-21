package kr.hhplus.be.adapter.out.persistence.repository

import kr.hhplus.be.adapter.out.persistence.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<UserEntity, Long> {
    fun findByUserId(userId: Long): UserEntity?
}
