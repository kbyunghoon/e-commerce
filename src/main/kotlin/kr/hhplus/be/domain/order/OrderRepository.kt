package kr.hhplus.be.domain.order

import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository {
    fun save(order: Order): Order
    fun findById(orderId: Long): Order?
    
    fun findByIdOrThrow(orderId: Long): Order {
        return findById(orderId) ?: throw BusinessException(ErrorCode.ORDER_NOT_FOUND)
    }
}
