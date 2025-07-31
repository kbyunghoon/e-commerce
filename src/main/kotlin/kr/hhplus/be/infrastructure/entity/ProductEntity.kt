package kr.hhplus.be.infrastructure.entity

import jakarta.persistence.*
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductStatus
import java.time.LocalDateTime

@Entity
@Table(name = "products")
class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    val id: Long = 0,

    @Column(name = "name", nullable = false)
    val name: String,

    @Column(name = "price", nullable = false)
    val price: Int,

    @Column(name = "stock", nullable = false)
    var stock: Int,

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    val status: ProductStatus,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Version
    var version: Long = 0L
) {
    fun toDomain(): Product {
        return Product(
            id = this.id,
            name = this.name,
            price = this.price,
            stock = this.stock,
            createdAt = this.createdAt,
            updatedAt = this.updatedAt,
            status = this.status,
            version = this.version
        )
    }

    companion object {
        fun from(product: Product): ProductEntity {
            return ProductEntity(
                id = product.id,
                name = product.name,
                price = product.price,
                stock = product.stock,
                status = product.status,
                createdAt = product.createdAt,
                updatedAt = product.updatedAt,
                version = product.version
            )
        }
    }
}