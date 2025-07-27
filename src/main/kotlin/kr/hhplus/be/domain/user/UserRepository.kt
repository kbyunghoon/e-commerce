package kr.hhplus.be.domain.user

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode

interface UserRepository {
    fun findById(id: Long): User?
    fun save(user: User): User
    fun findByEmail(email: String): User?
    
    fun findByIdOrThrow(id: Long): User {
        return findById(id) ?: throw BusinessException(ErrorCode.USER_NOT_FOUND)
    }
}
