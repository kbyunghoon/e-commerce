package kr.hhplus.be.domain.order

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kr.hhplus.be.domain.exception.BusinessException
import kr.hhplus.be.domain.exception.ErrorCode

class OrderItemTest : FunSpec({

    context("OrderItem 생성 테스트") {
        
        test("정상적인 주문 항목을 생성할 수 있다") {
            // Given
            val productId = 1L
            val quantity = 5
            val productName = "아이폰"
            val pricePerItem = 10000
            val orderId = 1L

            // When
            val orderItem = OrderItem.create(
                productId = productId,
                quantity = quantity,
                productName = productName,
                pricePerItem = pricePerItem,
                orderId = orderId
            )

            // Then
            orderItem.productId shouldBe productId
            orderItem.quantity shouldBe quantity
            orderItem.productName shouldBe productName
            orderItem.pricePerItem shouldBe pricePerItem
            orderItem.orderId shouldBe orderId
            orderItem.status shouldBe OrderStatus.PENDING
        }

        test("상품명이 포함된 주문 항목을 생성할 수 있다") {
            // Given
            val productName = "갤럭시 S24 Ultra"

            // When
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = productName,
                pricePerItem = 150000,
                orderId = 1L
            )

            // Then
            orderItem.productName shouldBe productName
        }

        test("빈 상품명으로도 주문 항목을 생성할 수 있다") {
            // Given
            val emptyProductName = ""

            // When
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 1,
                productName = emptyProductName,
                pricePerItem = 10000,
                orderId = 1L
            )

            // Then
            orderItem.productName shouldBe emptyProductName
        }

        test("긴 상품명으로도 주문 항목을 생성할 수 있다") {
            // Given
            val longProductName = "아주 긴 상품명을 가진 제품입니다. 이 제품은 매우 특별한 기능을 가지고 있으며, 고객들에게 최고의 만족을 제공합니다."

            // When
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 1,
                productName = longProductName,
                pricePerItem = 10000,
                orderId = 1L
            )

            // Then
            orderItem.productName shouldBe longProductName
        }

        test("상품 ID가 0 이하이면 예외가 발생한다") {
            // Given
            val invalidProductId = 0L

            // When & Then
            shouldThrow<BusinessException> {
                OrderItem.create(
                    productId = invalidProductId,
                    quantity = 1,
                    productName = "상품A",
                    pricePerItem = 10000,
                    orderId = 1L
                )
            }.errorCode shouldBe ErrorCode.INVALID_PRODUCT_ID
        }

        test("상품 ID가 음수이면 예외가 발생한다") {
            // Given
            val invalidProductId = -1L

            // When & Then
            shouldThrow<BusinessException> {
                OrderItem.create(
                    productId = invalidProductId,
                    quantity = 1,
                    productName = "상품A",
                    pricePerItem = 10000,
                    orderId = 1L
                )
            }.errorCode shouldBe ErrorCode.INVALID_PRODUCT_ID
        }

        test("수량이 최소값보다 작으면 예외가 발생한다") {
            // Given
            val invalidQuantity = 0

            // When & Then
            shouldThrow<BusinessException> {
                OrderItem.create(
                    productId = 1L,
                    quantity = invalidQuantity,
                    productName = "상품A",
                    pricePerItem = 10000,
                    orderId = 1L
                )
            }.errorCode shouldBe ErrorCode.INVALID_ORDER_QUANTITY
        }

        test("수량이 최대값보다 크면 예외가 발생한다") {
            // Given
            val invalidQuantity = 101

            // When & Then
            shouldThrow<BusinessException> {
                OrderItem.create(
                    productId = 1L,
                    quantity = invalidQuantity,
                    productName = "상품A",
                    pricePerItem = 10000,
                    orderId = 1L
                )
            }.errorCode shouldBe ErrorCode.INVALID_ORDER_QUANTITY
        }

        test("개별 가격이 최소값보다 작으면 예외가 발생한다") {
            // Given
            val invalidPrice = 0

            // When & Then
            shouldThrow<BusinessException> {
                OrderItem.create(
                    productId = 1L,
                    quantity = 1,
                    productName = "상품A",
                    pricePerItem = invalidPrice,
                    orderId = 1L
                )
            }.errorCode shouldBe ErrorCode.INVALID_PRODUCT_PRICE
        }

        test("개별 가격이 최대값보다 크면 예외가 발생한다") {
            // Given
            val invalidPrice = 100_000_001

            // When & Then
            shouldThrow<BusinessException> {
                OrderItem.create(
                    productId = 1L,
                    quantity = 1,
                    productName = "상품A",
                    pricePerItem = invalidPrice,
                    orderId = 1L
                )
            }.errorCode shouldBe ErrorCode.INVALID_PRODUCT_PRICE
        }
    }

    context("OrderItem 상태 변경 테스트") {
        
        test("대기 상태의 주문 항목을 완료할 수 있다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "노트북",
                pricePerItem = 10000,
                orderId = 1L
            )

            // When
            orderItem.completeOrder()

            // Then
            orderItem.status shouldBe OrderStatus.COMPLETED
            orderItem.isCompleted() shouldBe true
            orderItem.isPending() shouldBe false
        }

        test("이미 완료된 주문 항목을 다시 완료하려고 하면 예외가 발생한다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "노트북",
                pricePerItem = 10000,
                orderId = 1L
            )
            orderItem.completeOrder()

            // When & Then
            shouldThrow<BusinessException> {
                orderItem.completeOrder()
            }.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
        }

        test("대기 상태의 주문 항목을 취소할 수 있다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "노트북",
                pricePerItem = 10000,
                orderId = 1L
            )

            // When
            orderItem.cancelOrder()

            // Then
            orderItem.status shouldBe OrderStatus.CANCELLED
            orderItem.isCancelled() shouldBe true
            orderItem.isPending() shouldBe false
        }

        test("완료된 주문 항목을 취소할 수 있다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "노트북",
                pricePerItem = 10000,
                orderId = 1L
            )
            orderItem.completeOrder()

            // When
            orderItem.cancelOrder()

            // Then
            orderItem.status shouldBe OrderStatus.CANCELLED
            orderItem.isCancelled() shouldBe true
            orderItem.isCompleted() shouldBe false
        }

        test("이미 취소된 주문 항목을 다시 취소하려고 하면 예외가 발생한다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "노트북",
                pricePerItem = 10000,
                orderId = 1L
            )
            orderItem.cancelOrder()

            // When & Then
            shouldThrow<BusinessException> {
                orderItem.cancelOrder()
            }.errorCode shouldBe ErrorCode.ORDER_ALREADY_CANCELLED
        }

        test("취소된 주문 항목을 완료하려고 하면 예외가 발생한다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "노트북",
                pricePerItem = 10000,
                orderId = 1L
            )
            orderItem.cancelOrder()

            // When & Then
            shouldThrow<BusinessException> {
                orderItem.completeOrder()
            }.errorCode shouldBe ErrorCode.ORDER_ALREADY_PROCESSED
        }
    }

    context("OrderItem 상태 확인 메서드 테스트") {
        
        test("새로 생성된 주문 항목은 대기 상태이다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "키보드",
                pricePerItem = 10000,
                orderId = 1L
            )

            // Then
            orderItem.isPending() shouldBe true
            orderItem.isCompleted() shouldBe false
            orderItem.isCancelled() shouldBe false
            orderItem.canBeCompleted() shouldBe true
            orderItem.canBeCancelled() shouldBe true
        }

        test("완료된 주문 항목의 상태를 올바르게 확인할 수 있다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "키보드",
                pricePerItem = 10000,
                orderId = 1L
            )
            orderItem.completeOrder()

            // Then
            orderItem.isPending() shouldBe false
            orderItem.isCompleted() shouldBe true
            orderItem.isCancelled() shouldBe false
            orderItem.canBeCompleted() shouldBe false
            orderItem.canBeCancelled() shouldBe true
        }

        test("취소된 주문 항목의 상태를 올바르게 확인할 수 있다") {
            // Given
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 2,
                productName = "키보드",
                pricePerItem = 10000,
                orderId = 1L
            )
            orderItem.cancelOrder()

            // Then
            orderItem.isPending() shouldBe false
            orderItem.isCompleted() shouldBe false
            orderItem.isCancelled() shouldBe true
            orderItem.canBeCompleted() shouldBe false
            orderItem.canBeCancelled() shouldBe false
        }
    }

    context("OrderItem 총 가격 계산 테스트") {
        
        test("총 가격을 올바르게 계산할 수 있다") {
            // Given
            val quantity = 3
            val pricePerItem = 15000
            val expectedTotalPrice = quantity * pricePerItem

            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = quantity,
                productName = "마우스",
                pricePerItem = pricePerItem,
                orderId = 1L
            )

            // When
            val totalPrice = orderItem.getTotalPrice()

            // Then
            totalPrice shouldBe expectedTotalPrice
        }

        test("수량이 1일 때 총 가격을 올바르게 계산할 수 있다") {
            // Given
            val quantity = 1
            val pricePerItem = 25000

            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = quantity,
                productName = "헤드폰",
                pricePerItem = pricePerItem,
                orderId = 1L
            )

            // When
            val totalPrice = orderItem.getTotalPrice()

            // Then
            totalPrice shouldBe pricePerItem
        }

        test("최대 수량일 때 총 가격을 올바르게 계산할 수 있다") {
            // Given
            val quantity = OrderItem.MAX_QUANTITY
            val pricePerItem = 1000

            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = quantity,
                productName = "케이블",
                pricePerItem = pricePerItem,
                orderId = 1L
            )

            // When
            val totalPrice = orderItem.getTotalPrice()

            // Then
            totalPrice shouldBe (quantity * pricePerItem)
        }

        test("고가 상품의 총 가격을 올바르게 계산할 수 있다") {
            // Given
            val quantity = 2
            val pricePerItem = 500000
            val expectedTotalPrice = 1000000

            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = quantity,
                productName = "고급 노트북",
                pricePerItem = pricePerItem,
                orderId = 1L
            )

            // When
            val totalPrice = orderItem.getTotalPrice()

            // Then
            totalPrice shouldBe expectedTotalPrice
        }
    }

    context("OrderItem 불변 조건 검증 테스트") {
        
        test("불변 조건을 위반하는 OrderItem 객체로 상태 변경 시 예외가 발생한다") {
            // Given - 잘못된 값으로 OrderItem 객체를 직접 생성
            val invalidOrderItem = OrderItem(
                id = 1L,
                orderId = 1L,
                productId = 1L,
                productName = "테스트 상품",
                quantity = 0, // 최소값 위반
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            // When & Then
            shouldThrow<BusinessException> {
                invalidOrderItem.completeOrder()
            }.errorCode shouldBe ErrorCode.INVALID_ORDER_QUANTITY
        }

        test("상품 ID가 0인 OrderItem으로 상태 변경 시 예외가 발생한다") {
            // Given
            val invalidOrderItem = OrderItem(
                id = 1L,
                orderId = 1L,
                productId = 0L, // 유효하지 않은 상품 ID
                productName = "테스트 상품",
                quantity = 1,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            // When & Then
            shouldThrow<BusinessException> {
                invalidOrderItem.completeOrder()
            }.errorCode shouldBe ErrorCode.INVALID_PRODUCT_ID
        }

        test("가격이 범위를 벗어난 OrderItem으로 상태 변경 시 예외가 발생한다") {
            // Given
            val invalidOrderItem = OrderItem(
                id = 1L,
                orderId = 1L,
                productId = 1L,
                productName = "테스트 상품",
                quantity = 1,
                pricePerItem = 0, // 최소값 위반
                status = OrderStatus.PENDING
            )

            // When & Then
            shouldThrow<BusinessException> {
                invalidOrderItem.completeOrder()
            }.errorCode shouldBe ErrorCode.INVALID_PRODUCT_PRICE
        }
    }

    context("OrderItem 경계값 테스트") {
        
        test("최소 유효 값으로 OrderItem을 생성할 수 있다") {
            // Given & When
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = OrderItem.MIN_QUANTITY,
                productName = "최소 수량 상품",
                pricePerItem = OrderItem.MIN_PRICE,
                orderId = 1L
            )

            // Then
            orderItem.productId shouldBe 1L
            orderItem.quantity shouldBe OrderItem.MIN_QUANTITY
            orderItem.pricePerItem shouldBe OrderItem.MIN_PRICE
            orderItem.productName shouldBe "최소 수량 상품"
        }

        test("최대 유효 값으로 OrderItem을 생성할 수 있다") {
            // Given & When
            val orderItem = OrderItem.create(
                productId = Long.MAX_VALUE,
                quantity = OrderItem.MAX_QUANTITY,
                productName = "최대 수량 상품",
                pricePerItem = OrderItem.MAX_PRICE,
                orderId = 1L
            )

            // Then
            orderItem.productId shouldBe Long.MAX_VALUE
            orderItem.quantity shouldBe OrderItem.MAX_QUANTITY
            orderItem.pricePerItem shouldBe OrderItem.MAX_PRICE
            orderItem.productName shouldBe "최대 수량 상품"
        }
    }

    context("OrderItem 불변성 테스트") {
        
        test("OrderItem 객체는 데이터 클래스로서 동등성을 지원한다") {
            // Given
            val orderItem1 = OrderItem(
                id = 1L,
                orderId = 1L,
                productId = 1L,
                productName = "동일 상품",
                quantity = 2,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            val orderItem2 = OrderItem(
                id = 1L,
                orderId = 1L,
                productId = 1L,
                productName = "동일 상품",
                quantity = 2,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            // When & Then
            orderItem1 shouldBe orderItem2
            orderItem1.hashCode() shouldBe orderItem2.hashCode()
        }

        test("서로 다른 OrderItem 객체는 동등하지 않다") {
            // Given
            val orderItem1 = OrderItem(
                productId = 1L,
                productName = "상품A",
                quantity = 2,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            val orderItem2 = OrderItem(
                productId = 2L,
                productName = "상품B",
                quantity = 2,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            // When & Then
            (orderItem1 == orderItem2) shouldBe false
        }
    }

    context("OrderItem copy 테스트") {
        
        test("OrderItem 객체를 copy하여 일부 필드를 변경할 수 있다") {
            // Given
            val originalItem = OrderItem(
                id = 1L,
                orderId = 1L,
                productId = 1L,
                productName = "원본 상품",
                quantity = 2,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            // When
            val modifiedItem = originalItem.copy(productName = "수정된 상품")

            // Then
            modifiedItem.id shouldBe originalItem.id
            modifiedItem.orderId shouldBe originalItem.orderId
            modifiedItem.productId shouldBe originalItem.productId
            modifiedItem.productName shouldBe "수정된 상품"
            modifiedItem.quantity shouldBe originalItem.quantity
            modifiedItem.pricePerItem shouldBe originalItem.pricePerItem
            modifiedItem.status shouldBe originalItem.status
        }

        test("OrderItem 객체를 copy하여 수량과 가격을 변경할 수 있다") {
            // Given
            val originalItem = OrderItem(
                productId = 1L,
                productName = "상품",
                quantity = 2,
                pricePerItem = 10000,
                status = OrderStatus.PENDING
            )

            val newQuantity = 5
            val newPrice = 15000

            // When
            val modifiedItem = originalItem.copy(
                quantity = newQuantity,
                pricePerItem = newPrice
            )

            // Then
            modifiedItem.productId shouldBe originalItem.productId
            modifiedItem.productName shouldBe originalItem.productName
            modifiedItem.quantity shouldBe newQuantity
            modifiedItem.pricePerItem shouldBe newPrice
            modifiedItem.status shouldBe originalItem.status
        }
    }

    context("OrderItem 실제 사용 시나리오 테스트") {
        
        test("전자제품 주문 시나리오") {
            // Given & When
            val laptopItem = OrderItem.create(
                productId = 1L,
                quantity = 1,
                productName = "MacBook Pro 16인치",
                pricePerItem = 3500000,
                orderId = 1L
            )

            // Then
            laptopItem.productName shouldBe "MacBook Pro 16인치"
            laptopItem.getTotalPrice() shouldBe 3500000
        }

        test("생활용품 대량 주문 시나리오") {
            // Given & When
            val toiletPaperItem = OrderItem.create(
                productId = 2L,
                quantity = 24,
                productName = "화장지 12롤 x 2팩",
                pricePerItem = 8000,
                orderId = 2L
            )

            // Then
            toiletPaperItem.quantity shouldBe 24
            toiletPaperItem.getTotalPrice() shouldBe 192000
        }

        test("주문 항목 처리 전체 플로우") {
            // Given - 주문 항목 생성
            val orderItem = OrderItem.create(
                productId = 3L,
                quantity = 3,
                productName = "무선 이어폰",
                pricePerItem = 150000,
                orderId = 3L
            )

            // When & Then - 초기 상태 확인
            orderItem.isPending() shouldBe true
            orderItem.getTotalPrice() shouldBe 450000

            // When - 주문 완료 처리
            orderItem.completeOrder()

            // Then - 완료 상태 확인
            orderItem.isCompleted() shouldBe true
            orderItem.isPending() shouldBe false
        }

        test("주문 취소 시나리오") {
            // Given
            val orderItem = OrderItem.create(
                productId = 4L,
                quantity = 2,
                productName = "스마트워치",
                pricePerItem = 300000,
                orderId = 4L
            )

            // When - 주문 취소
            orderItem.cancelOrder()

            // Then - 취소 상태 확인
            orderItem.isCancelled() shouldBe true
            orderItem.canBeCompleted() shouldBe false
            orderItem.canBeCancelled() shouldBe false
        }
    }

    context("OrderItem 상품명 관련 테스트") {
        
        test("특수문자가 포함된 상품명으로 주문 항목을 생성할 수 있다") {
            // Given
            val specialProductName = "iPhone 15 Pro (512GB) - 스페이스 블랙 [정품]"

            // When
            val orderItem = OrderItem.create(
                productId = 1L,
                quantity = 1,
                productName = specialProductName,
                pricePerItem = 1500000,
                orderId = 1L
            )

            // Then
            orderItem.productName shouldBe specialProductName
        }

        test("숫자가 포함된 상품명으로 주문 항목을 생성할 수 있다") {
            // Given
            val numericProductName = "갤럭시 S24 Ultra 256GB"

            // When
            val orderItem = OrderItem.create(
                productId = 2L,
                quantity = 1,
                productName = numericProductName,
                pricePerItem = 1400000,
                orderId = 1L
            )

            // Then
            orderItem.productName shouldBe numericProductName
        }

        test("영어 상품명으로 주문 항목을 생성할 수 있다") {
            // Given
            val englishProductName = "AirPods Pro 2nd Generation"

            // When
            val orderItem = OrderItem.create(
                productId = 3L,
                quantity = 2,
                productName = englishProductName,
                pricePerItem = 350000,
                orderId = 1L
            )

            // Then
            orderItem.productName shouldBe englishProductName
        }
    }
})
