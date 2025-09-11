package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.infrastructure.entity.UserEntity
import kr.hhplus.be.infrastructure.persistence.repository.jpa.UserJpaRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(id: Long): User? {
        return userJpaRepository.findByIdOrNull(id)?.toDomain()
    }

    override fun save(user: User): User {
        return userJpaRepository.save(UserEntity.from(user)).toDomain()
    }
}
