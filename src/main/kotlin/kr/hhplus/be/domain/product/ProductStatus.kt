package kr.hhplus.be.domain.product

enum class ProductStatus(val value: String) {
    ACTIVE("활성화"),
    INACTIVE("비활성화"),
    OUT_OF_STOCK("품절")
}
