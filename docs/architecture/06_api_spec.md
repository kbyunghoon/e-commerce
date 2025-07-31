# API 명세서

## 잔액 관리 API

### 잔액 충전

```http request
POST /api/v1/balance/charge
Content-Type: application/json
```

Request Body

```json
{
  "userId": 1,
  "amount": 10000
}
```

| 필드     | 타입   | 설명         | 필수 |
|--------|------|------------|----|
| userId | long | 사용자 ID     | ✅  |
| amount | int  | 충전할 금액 (원) | ✅  |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "balance": 20000,
    "chargedAmount": 10000,
    "chargedAt": "2025-07-16T00:00:00"
  }
}
```

#### Response: 실패 (400 Bad Request)

```json
{
  "success": false,
  "error": {
    "code": "INVALID_AMOUNT",
    "message": "충전 금액은 0보다 커야 합니다."
  }
}
```

### 잔액 조회

```http request
GET /api/v1/balance?userId={userId}
```

Query Parameters

| 필드     | 타입   | 설명     | 필수 |
|--------|------|--------|----|
| userId | long | 사용자 ID | ✅  |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "balance": 20000,
    "lastUpdatedAt": "2025-07-16T00:00:00"
  }
}
```

#### Response: 실패 (404 Not Found)

```json
{
  "success": false,
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "사용자를 찾을 수 없습니다."
  }
}
```

## 상품 관리 API

### 상품 목록 조회

```http request
GET /api/v1/products
```

Query Parameters

| 필드     | 타입     | 설명     | 필수 | 기본값    |
|--------|--------|--------|----|--------|
| page   | int    | 페이지 번호 | ❌  | 0 or 1 |
| size   | int    | 페이지 크기 | ❌  | 10     |
| search | string | 상품명 검색 | ❌  | -      |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "상품명",
        "price": 10000,
        "stock": 150,
        "createdAt": "2025-07-16T00:00:00",
        "updatedAt": "2025-07-16T00:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 1,
      "totalElements": 3,
      "totalPages": 3,
      "hasNext": true,
      "hasPrevious": false
    }
  }
}
```

#### Response: 실패 (401 Unauthorized)

```json
{
  "success": false,
  "error": {
    "code": "UNAUTHORIZED",
    "message": "인증이 필요합니다."
  }
}
```

### 상품 상세 조회

```http request
GET /api/v1/products/{productId}
```

Path Parameters

| 필드        | 타입   | 설명    | 필수 |
|-----------|------|-------|----|
| productId | long | 상품 ID | ✅  |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "id": 1,
    "name": "상품명",
    "price": 89000,
    "stock": 150,
    "createdAt": "2025-07-16T00:00:00",
    "updatedAt": "2025-07-16T00:00:00"
  }
}
```

#### Response: 실패 (404 Not Found)

```json
{
  "success": false,
  "error": {
    "code": "PRODUCT_NOT_FOUND",
    "message": "상품을 찾을 수 없습니다."
  }
}
```

## 선착순 쿠폰 API

### 선착순 쿠폰 발급

```http request
POST /api/v1/coupons/issue
Content-Type: application/json
```

Request Body

```json
{
  "userId": 1,
  "couponId": 1
}
```

| 필드       | 타입   | 설명     | 필수 |
|----------|------|--------|----|
| userId   | long | 사용자 ID | ✅  |
| couponId | long | 쿠폰 ID  | ✅  |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "userCouponId": 1,
    "userId": 1,
    "couponId": 1,
    "couponName": "10% 할인 쿠폰",
    "discountType": "PERCENTAGE",
    "discountValue": 10,
    "expiresAt": "2025-08-16T23:59:59",
    "issuedAt": "2025-07-16T00:00:00"
  }
}
```

#### Response: 실패 (400 Bad Request)

```json
{
  "success": false,
  "error": {
    "code": "COUPON_SOLD_OUT",
    "message": "쿠폰이 모두 소진되었습니다."
  }
}
```

#### Response: 실패 (409 Conflict)

```json
{
  "success": false,
  "error": {
    "code": "COUPON_ALREADY_ISSUED",
    "message": "이미 발급받은 쿠폰입니다."
  }
}
```

### 보유 쿠폰 조회

```http request
GET /api/v1/coupons?userId={userId}
```

Query Parameter

| 필드 | 타입 | 설명 | 필수 |
|--------|-----|---------| |
| userId | long | 사용자 ID | ✅ |
| status | string | 쿠폰 상태 필터 | ❌ |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "coupons": [
      {
        "userCouponId": 1,
        "couponId": 1,
        "couponName": "10% 할인 쿠폰",
        "discountType": "PERCENTAGE",
        "discountValue": 10,
        "status": "AVAILABLE",
        "expiryDate": "2025-08-16T23:59:59",
        "issuedAt": "2025-07-16T00:00:00"
      }
    ]
  }
}
```

## 주문 및 결제 API

### 주문 생성 (결제 전)

```http request
POST /api/v1/orders
Content-Type: application/json
```

Request Body

```json
{
  "userId": 1,
  "items": [
    {
      "productId": 1,
      "quantity": 2
    }
  ],
  "couponId": 1
}
```

| 필드 | 타입 | 설명 | 필수 |
|--------|-----|---------| |
| userId | long | 사용자 ID | ✅ |
| items | array | 주문 상품 목록 | ✅ |
| couponId | long | 사용할 쿠폰 ID | ❌ |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "orderId": "주문번호",
    "userId": 1,
    "items": [
      {
        "productId": 1,
        "productName": "상품명",
        "price": 10000,
        "quantity": 2
      }
    ],
    "originalAmount": 20000,
    "discountAmount": 2000,
    "finalAmount": 18000
  }
}
```

### 결제 처리

```http request
POST /api/v1/orders/{orderId}/pay
Content-Type: application/json
```

Path Parameters

| 필드      | 타입     | 설명                    | 필수 |
|---------|--------|-----------------------|----|
| orderId | String | '주문 생성'시 발급된 임시 주문 ID | ✅  |

Request Body

```json
{
  "userId": 1,
  "paymentMethod": "BALANCE"
}
```

| 필드            | 타입     | 설명     | 필수 |
|---------------|--------|--------|----|
| userId        | long   | 사용자 ID | ✅  |
| paymentMethod | String | 결제 방식  | ✅  |

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "orderId": 12345,
    "orderNumber": "주문 번호",
    "userId": 1,
    "finalAmount": 18000,
    "status": "COMPLETED",
    "orderedAt": "2025-07-16T21:30:00"
  }
}
```

#### Response: 실패 (402 Payment Required)

```json
{
  "success": false,
  "error": {
    "code": "INSUFFICIENT_BALANCE",
    "message": "잔액이 부족합니다."
  }
}
```

## 통계 API

### 인기 상품 조회

```http request
GET /api/v1/products/top
```

#### Response: 성공 (200 OK)

```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": 1,
        "name": "상품명",
        "price": 10000,
        "totalSalesQuantity": 150,
        "rank": 1
      }
    ]
  }
}
```