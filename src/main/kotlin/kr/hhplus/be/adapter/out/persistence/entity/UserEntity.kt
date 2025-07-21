package kr.hhplus.be.adapter.out.persistence.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.model.User
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val userId: Long = 0,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "email", nullable = false, unique = true)
    val email: String,

    @Column(name = "balance", nullable = false)
    var balance: Int = 0,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): User {
        return User(
            userId = this.userId,
            name = this.name,
            email = this.email,
            balance = this.balance,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }

    companion object {
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                userId = user.userId,
                name = user.name,
                email = user.email,
                balance = user.balance,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )
        }
    }
}
