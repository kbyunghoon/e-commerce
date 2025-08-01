package kr.hhplus.be.domain.product


interface ProductStockHistoryRepository {
    fun save(productHistory: ProductStockHistory): ProductStockHistory
}
