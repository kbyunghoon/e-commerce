package kr.hhplus.be.presentation.dto.request

import jakarta.validation.constraints.Min
import kr.hhplus.be.application.product.ProductSearchCommand
import org.springframework.data.domain.Pageable

data class ProductSearchRequest(
    val search: String? = null,

    @field:Min(0, message = "최소 가격은 0 이상이어야 합니다")
    val minPrice: Int? = null,

    @field:Min(0, message = "최대 가격은 0 이상이어야 합니다")
    val maxPrice: Int? = null
) {
    fun toCommand(pageable: Pageable): ProductSearchCommand {
        return ProductSearchCommand(
            pageable = pageable,
            search = search,
            minPrice = minPrice,
            maxPrice = maxPrice
        )
    }
}
