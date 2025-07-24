package kr.hhplus.be.domain.user

interface UserRepository {
    fun findById(id: Long): User?
    fun save(user: User): User
    fun findByEmail(email: String): User?
}
