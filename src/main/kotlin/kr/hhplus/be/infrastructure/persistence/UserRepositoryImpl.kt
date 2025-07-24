package kr.hhplus.be.infrastructure.persistence

import kr.hhplus.be.domain.user.User
import kr.hhplus.be.domain.user.UserRepository
import kr.hhplus.be.infrastructure.persistence.repository.UserJpaRepository
import org.springframework.stereotype.Component

@Component
class UserRepositoryImpl(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {
    override fun findById(id: Long): User? {
        TODO("구현 예정")
    }

    override fun save(user: User): User {
        TODO("구현 예정")
    }

    override fun findByEmail(email: String): User? {
        TODO("구현 예정")
    }
}
