package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.user.User
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class UserEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    val id: Long = 0,

    @Column(name = "balance", nullable = false)
    var balance: Int = 0,

    @Column(name = "name", nullable = false)
    var name: String = "",

    @Column(name = "email", nullable = false)
    var email: String = "",

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun toDomain(): User {
        return User(
            id = this.id,
            balance = this.balance,
            name = this.name,
            email = this.email,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt
        )
    }
}