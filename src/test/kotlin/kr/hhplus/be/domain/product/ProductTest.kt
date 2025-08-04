import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import kr.hhplus.be.domain.product.Product
import kr.hhplus.be.domain.product.ProductStatus
import java.time.LocalDateTime

class ProductTest : FunSpec({

    test("재고 범위 내에서 검증하면 예외가 발생하지 않는다") {
        val product = createProduct()
        product.validateStock(5)
        product.validateStock(10)
    }

    test("재고보다 많은 수량으로 검증하면 예외가 발생한다") {
        val product = createProduct()
        shouldThrow<BusinessException> {
            product.validateStock(15)
        }.errorCode shouldBe ErrorCode.INSUFFICIENT_STOCK
    }

    test("재고 범위 내에서 차감하면 재고가 감소한다") {
        val product = createProduct()
        product.deductStock(3)
        product.stock shouldBe 7
    }

    test("재고보다 많은 수량을 차감하면 예외가 발생한다") {
        val product = createProduct()
        shouldThrow<BusinessException> {
            product.deductStock(15)
        }.errorCode shouldBe ErrorCode.INSUFFICIENT_STOCK
    }

    test("연속으로 재고를 차감하면 누적 차감된다") {
        val product = createProduct()
        product.deductStock(3)
        product.deductStock(2)
        product.stock shouldBe 5
    }

    test("재고를 모두 차감하면 재고가 0이 된다") {
        val product = createProduct()
        product.deductStock(10)
        product.stock shouldBe 0
    }

}) {
    companion object {
        private fun createProduct() = Product(
            id = 1L,
            name = "테스트 상품",
            stock = 10,
            price = 50000,
            status = ProductStatus.ACTIVE,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
    }
}
