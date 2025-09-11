package kr.hhplus.be.infrastructure.persistence.repository.jpa

import kr.hhplus.be.infrastructure.entity.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserJpaRepository : JpaRepository<UserEntity, Long> {
}