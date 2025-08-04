# DB 성능 최적화 보고서

## 📑 목차

- [요약](#요약)
- [병목 예상 쿼리 분석](#병목-예상-쿼리-분석)
- [개선 사항](#개선-사항)
    - [2.1. 주문 조회 최적화](#21-주문-조회-최적화)
        - [2.1.1. user_id 기준 조회](#211-user_id-기준-조회)
        - [2.1.2. user_id 및 status 기준 조회 (복합 인덱스)](#212-user_id-및-status-기준-조회-복합-인덱스)
    - [2.2. 유저 쿠폰 조회 최적화](#22-유저-쿠폰-조회-최적화)
        - [2.2.1. user_id 기준 조회](#221-user_id-기준-조회)
        - [2.2.2. coupon_id 기준 조회](#222-coupon_id-기준-조회)
        - [2.2.3. user_id 및 status 기준 조회 (복합 인덱스)](#223-user_id-및-status-기준-조회-복합-인덱스)
    - [2.3. 잔액 이력 조회 최적화](#23-잔액-이력-조회-최적화)
        - [2.3.1. user_id 기준 조회](#231-user_id-기준-조회)
        - [2.3.2. user_id 및 type 기준 조회 (복합 인덱스)](#232-user_id-및-type-기준-조회-복합-인덱스)
    - [2.4. 주문 상품 조회 최적화](#24-주문-상품-조회-최적화)
        - [2.4.1. order_id 기준 조회](#241-order_id-기준-조회)
        - [2.4.2. product_id 기준 조회](#242-product_id-기준-조회)

## 요약

**각 테이블 당 10만개의 더미데이터 기준**으로 주문 조회, 유저 쿠폰 조회, 잔액 이력 조회, 주문 상품 조회 등 주요 쿼리에서 풀 테이블 스캔(Full Table Scan)을 효율적인 인덱스 탐색으로 전환하여 쿼리 시간 전/후를 비교해보았습니다.<br/>
각 개선 사례별로 인덱스 적용 전/후 쿼리 플랜을 통해 성능 향상 효과를 작성하였습니다.

| 기능                             | 인덱스 전 쿼리 시간 | 인덱스 후 쿼리 시간 | 개선율         | 비고                           |
|--------------------------------|-------------|-------------|-------------|------------------------------|
| 주문 조회 (`user_id`)              | 34.5ms      | 0.169ms     | 약 99.51% 개선 | 단일 인덱스 (`user_id`)           |
| 주문 조회 (`user_id`, `status`)    | 46.8ms      | 0.318ms     | 약 99.32% 개선 | 복합 인덱스 (`user_id`, `status`) |
| 유저 쿠폰 조회 (`user_id`)           | 32ms        | 0.201ms     | 약 99.37% 개선 | 단일 인덱스 (`user_id`)           |
| 유저 쿠폰 조회 (`coupon_id`)         | 20.2ms      | 5.84ms      | 약 71.08% 개선 | 단일 인덱스 (`coupon_id`)         |
| 유저 쿠폰 조회 (`user_id`, `status`) | 5.47ms      | 1.19ms      | 약 78.24% 개선 | 복합 인덱스 (`user_id`, `status`) |
| 잔액 조회 (`user_id`)              | 22.5ms      | 2.8ms       | 약 87.56% 개선 | 단일 인덱스 (`user_id`)           |
| 잔액 조회 (`user_id`, `type`)      | 1.7ms       | 0.299ms     | 약 82.41% 개선 | 복합 인덱스 (`user_id`, `type`)   |
| 주문 상품 조회 (`order_id`)          | 25.1ms      | 0.118ms     | 약 99.53% 개선 | 단일 인덱스 (`order_id`)          |
| 주문 상품 조회 (`product_id`)        | 28ms        | 4.67ms      | 약 83.32% 개선 | 단일 인덱스 (`product_id`)        |

## 병목 예상 쿼리 분석

### 🎯 서비스 병목 쿼리 식별 및 분석

#### 1. **주문 조회 기능** - 사용자별 주문 내역 조회

**병목 예상 시나리오**
```sql
-- 사용자가 본인 주문 내역을 조회하는 쿼리
SELECT * FROM orders WHERE user_id = 87328;
```

**병목 분석**
- **예상 문제점**: 사용자 수 증가 시 orders 테이블 풀 스캔으로 인한 성능 저하
- **트래픽 패턴**: 사용자들이 주문 내역을 자주 조회하는 핵심 기능
- **데이터 규모**: orders 테이블에 10만개 레코드 존재

**실제 성능 측정 (Before)**
```text
-> Filter: (orders.user_id = 87328)  (cost=10122 rows=9954) (actual time=0.198..34.5 rows=1 loops=1)
    -> Table scan on orders  (cost=10122 rows=99540) (actual time=0.186..30 rows=100000 loops=1)
```
- **실행 시간**: 34.5ms
- **스캔 방식**: Full Table Scan (전체 테이블 스캔)
- **비용**: 10122

#### 2. **유저 쿠폰 조회 기능** - 특정 쿠폰의 발급 현황 조회

**병목 예상 시나리오**
```sql
-- 관리자가 특정 쿠폰이 얼마나 발급되었는지 확인하는 쿼리
SELECT * FROM user_coupons WHERE coupon_id = 1;
```

**병목 분석**
- **예상 문제점**: 인기 쿠폰의 발급 현황 조회 시 대량 데이터 스캔
- **데이터 특성**: 한 쿠폰당 수천 명의 사용자가 발급받을 수 있음 (카디널리티 높음)

**실제 성능 측정 (Before)**
```text
-> Filter: (user_coupons.coupon_id = 1)  (cost=10090 rows=9986) (actual time=0.925..23.5 rows=2001 loops=1)
    -> Table scan on user_coupons  (cost=10090 rows=99855) (actual time=0.89..20.2 rows=100000 loops=1)
```
- **실행 시간**: 20.2ms
- **결과 레코드**: 2,001개 (높은 카디널리티)
- **문제**: 2,001개 찾기 위해 100,000개 전체 스캔

#### 3. **복합 조건 쿼리** - 사용자별 사용 가능한 쿠폰 조회

**병목 예상 시나리오**
```sql
-- 사용자가 결제 시 사용할 수 있는 쿠폰 목록을 조회
SELECT * FROM user_coupons WHERE user_id = 870 AND status = 'AVAILABLE';
```

**병목 분석**
- **예상 문제점**: 결제 프로세스 중 실시간 쿠폰 조회로 인한 지연
- **쿼리 패턴**: 두 개 조건을 동시에 사용하는 복합 조건, 인덱스 효율성 중요

**실제 성능 측정 (Before)**
```text
-> Filter: (user_coupons.`status` = 'AVAILABLE')  (cost=32.6 rows=38.3) (actual time=4.96..5.47 rows=65 loops=1)
    -> Index lookup on user_coupons using idx_user_id (user_id=870)  (cost=32.6 rows=115) (actual time=4.93..5.43 rows=115 loops=1)
```
- **문제**: user_id 인덱스 후 추가 필터링으로 비효율 발생
- **실행 시간**: 5.47ms
- **비효율**: 115개 검색 후 65개로 필터링

#### 4. **주문 상세 조회** - 주문 상품 목록 조회

**병목 예상 시나리오**
```sql
-- 특정 주문의 상품 목록을 조회하는 쿼리
SELECT * FROM order_items WHERE order_id = 93947;
```

**병목 분석**
- **예상 문제점**: 주문 상세 페이지 로딩 지연

**실제 성능 측정 (Before)**
```text
-> Filter: (order_items.order_id = 93947)  (cost=10094 rows=9958) (actual time=0.189..28.6 rows=2 loops=1)
    -> Table scan on order_items  (cost=10094 rows=99575) (actual time=0.173..25.1 rows=100000 loops=1)
```
- **실행 시간**: 25.1ms
- **비효율**: 2개 결과를 위해 100,000개 레코드 스캔

### 📊 병목 쿼리 우선순위 매트릭스

| 순위 | 쿼리 기능 | 실행 시간 | 사용 빈도 | 비즈니스 임팩트 | 개선 우선도 |
|------|-----------|-----------|-----------|----------------|-------------|
| 1 | 주문 조회 (user_id) | 34.5ms | **매우 높음** | **매우 높음** (핵심 기능) | 🔴 최우선 |
| 2 | 주문 상품 조회 (order_id) | 25.1ms | **높음** | **높음** (주문 상세) | 🔴 최우선 |
| 3 | 잔액 조회 (user_id) | 22.5ms | **매우 높음** | **매우 높음** (결제 관련) | 🔴 최우선 |
| 4 | 유저 쿠폰 조회 (coupon_id) | 20.2ms | **중간** | **중간** (관리 기능) | 🟡 중간 |
| 5 | 복합 조건 쿠폰 조회 | 5.47ms | **높음** | **높음** (결제 흐름) | 🟠 높음 |

## 개선 사항

### 2.1. 주문 조회 최적화

#### 2.1.1. `user_id` 기준 조회

사용자 ID(`user_id`) 기준 주문 조회 쿼리

#### 인덱스 적용 전

`user_id` 컬럼에 인덱스가 없어 `orders` 테이블 전체를 스캔하는 비효율적인 방식으로 동작했습니다.<br/>
대량의 데이터일수록 높은 비용과 긴 실행 시간이 발생하였습니다.

```text
-> Filter: (orders.user_id = 87328)  (cost=10122 rows=9954) (actual time=0.198..34.5 rows=1 loops=1)
    -> Table scan on orders  (cost=10122 rows=99540) (actual time=0.186..30 rows=100000 loops=1)
```

#### 인덱스 생성

`orders` 테이블의 `user_id` 컬럼에 인덱스를 생성하여 특정 `user_id`를 가진 주문을 빠르게 찾을 수 있도록 했습니다.

```sql
CREATE INDEX idx_user_id ON orders (user_id);
```

#### 인덱스 적용 후

`user_id`를 통한 조회 시 풀 테이블 스캔 대신 `idx_user_id` 인덱스를 활용하여 매우 낮은 비용과 빠른 실행 시간으로 데이터를 조회할 수 있게 되었습니다.<br/>
**약 34.5ms -> 0.169ms (약 99.51% 개선)**

```text
-> Index lookup on orders using idx_orders_user_status (user_id=87328)  (cost=0.35 rows=1) (actual time=0.161..0.169 rows=1 loops=1)
```

#### 2.1.2. `user_id` 및 `status` 기준 조회 (복합 인덱스)

사용자 ID(`user_id`)와 주문 상태(`status`) 기준 주문 조회 쿼리

#### 인덱스 적용 전

`user_id`와 `status` 컬럼에 대한 복합 인덱스가 없어 `orders` 테이블 전체를 스캔하는 비효율적인 방식으로 동작했습니다.

```text
-> Filter: ((orders.`status` = 'COMPLETED') and (orders.user_id = 87328))  (cost=10122 rows=3318) (actual time=1.88..56.4 rows=1 loops=1)
    -> Table scan on orders  (cost=10122 rows=99540) (actual time=1.8..46.8 rows=100000 loops=1)
```

#### 인덱스 생성

`orders` 테이블의 `user_id`와 `status` 컬럼에 복합 인덱스를 생성하여 특정 `user_id`와 `status`를 가진 주문을 빠르게 찾을 수 있도록 했습니다.

```sql
CREATE INDEX idx_orders_user_status ON orders (user_id, status);
```

#### 인덱스 적용 후

복합 인덱스 생성 후, `user_id`와 `status`를 통한 조회 시 인덱스를 활용하여 매우 낮은 비용과 빠른 실행 시간으로 데이터를 조회할 수 있게 되었습니다.<br/>
**약 46.8ms -> 0.318ms (약 99.32% 개선)**

```text
-> Index lookup on orders using idx_orders_user_status (user_id=87328, status='COMPLETED'), with index condition: (orders.`status` = 'COMPLETED')  (cost=0.35 rows=1) (actual time=0.309..0.318 rows=1 loops=1)
```

### 2.2. 유저 쿠폰 조회 최적화

#### 2.2.1. `user_id` 기준 조회

특정 사용자(`user_id`)가 보유한 쿠폰 목록 조회 쿼리

#### 인덱스 적용 전

`user_id` 컬럼에 인덱스가 없어 `user_coupons` 테이블 전체를 스캔하여 데이터를 필터링했습니다.

```text
-> Filter: (user_coupons.user_id = 1)  (cost=10090 rows=9986) (actual time=1.57..35.5 rows=107 loops=1)
    -> Table scan on user_coupons  (cost=10090 rows=99855) (actual time=1.53..32 rows=100000 loops=1)
```

#### 인덱스 생성

`user_coupons` 테이블의 `user_id` 컬럼에 인덱스를 생성했습니다.

```sql
CREATE INDEX idx_user_id ON user_coupons (user_id);
```

#### 인덱스 적용 후

**약 32ms -> 0.201ms (약 99.37% 개선)**

```text
-> Index lookup on user_coupons using idx_user_id (user_id=1)  (cost=37.5 rows=107) (actual time=0.179..0.201 rows=107 loops=1)
```

#### 2.2.2. `coupon_id` 기준 조회

특정 쿠폰(`coupon_id`)이 사용자들에게 어떻게 발급되었는지 조회하는 쿼리

#### 인덱스 적용 전

`coupon_id` 컬럼에 인덱스가 없어 `user_coupons` 테이블 전체를 스캔했습니다.

```text
-> Filter: (user_coupons.coupon_id = 1)  (cost=10090 rows=9986) (actual time=0.925..23.5 rows=2001 loops=1)
    -> Table scan on user_coupons  (cost=10090 rows=99855) (actual time=0.89..20.2 rows=100000 loops=1)
```

#### 인덱스 생성

`user_coupons` 테이블의 `coupon_id` 컬럼에 인덱스를 생성했습니다.

```sql
CREATE INDEX idx_coupon_id ON user_coupons (coupon_id);
```

#### 인덱스 적용 후

**약 20.2ms -> 5.84ms (약 71.08% 개선)**

```text
-> Index lookup on user_coupons using idx_coupon_id (coupon_id=1)  (cost=513 rows=2001) (actual time=1.17..5.84 rows=2001 loops=1)
```

#### 2.2.3. `user_id` 및 `status` 기준 조회 (복합 인덱스)

특정 사용자의 특정 상태(`status`)를 가진 쿠폰 조회 쿼리

#### 인덱스 적용 전

```text
-> Filter: (user_coupons.`status` = 'AVAILABLE')  (cost=32.6 rows=38.3) (actual time=4.96..5.47 rows=65 loops=1)
    -> Index lookup on user_coupons using idx_user_id (user_id=870)  (cost=32.6 rows=115) (actual time=4.93..5.43 rows=115 loops=1)
```

#### 인덱스 생성

`user_id`와 `status` 컬럼을 포함하는 복합 인덱스 `idx_user_coupons_user_status`를 생성했습니다.

```sql
CREATE INDEX idx_user_coupons_user_status ON user_coupons (user_id, status);
```

#### 인덱스 적용 후

**약 5.47ms -> 1.19ms (약 78.24% 개선)**

```text
-> Index lookup on user_coupons using idx_user_coupons_user_status (user_id=870, status='AVAILABLE'), with index condition: (user_coupons.`status` = 'AVAILABLE')  (cost=22.8 rows=65) (actual time=1.18..1.19 rows=65 loops=1)
```

### 2.3. 잔액 이력 조회 최적화

#### 2.3.1. `user_id` 기준 조회

사용자 ID(`user_id`) 기준 잔액 이력 조회 쿼리

#### 인덱스 적용 전

```text
-> Filter: (balance_history.user_id = 1)  (cost=10098 rows=10010) (actual time=1.25..25.9 rows=97 loops=1)
    -> Table scan on balance_history  (cost=10098 rows=100099) (actual time=1.07..22.5 rows=100000 loops=1)
```

#### 인덱스 생성

```sql
CREATE INDEX idx_balance_history_user_id ON balance_history (user_id);
```

#### 인덱스 적용 후

**약 22.5ms -> 2.8ms (약 87.56% 개선)**

```text
-> Index lookup on balance_history using idx_balance_history_user_id (user_id=1)  (cost=34 rows=97) (actual time=2.79..2.8 rows=97 loops=1)
```

#### 2.3.2. `user_id` 및 `type` 기준 조회 (복합 인덱스)

사용자 ID(`user_id`)와 거래 유형(`type`) 기준 잔액 이력 조회 쿼리

#### 인덱스 적용 전

```text
-> Filter: (balance_history.`type` = 'DEDUCT')  (cost=27.5 rows=32.3) (actual time=1.67..1.7 rows=50 loops=1)
    -> Index lookup on balance_history using idx_balance_history_user_id (user_id=1), with index condition: (balance_history.user_id = 1)  (cost=27.5 rows=97) (actual time=1.33..1.35 rows=97 loops=1)
```

#### 인덱스 생성

```sql
CREATE INDEX idx_balance_history_user_type ON balance_history (user_id, type);
```

#### 인덱스 적용 후

**약 1.7ms -> 0.299ms (약 82.41% 개선)**

```text
-> Index lookup on balance_history using idx_balance_history_user_type (user_id=1, type='DEDUCT'), with index condition: ((balance_history.`type` = 'DEDUCT') and (balance_history.user_id = 1))  (cost=17.5 rows=50) (actual time=0.289..0.299 rows=50 loops=1)
```

### 2.4. 주문 상품 조회 최적화

#### 2.4.1. `order_id` 기준 조회

주문 ID(`order_id`) 기준 주문 상품 조회 쿼리

#### 인덱스 적용 전

```text
-> Filter: (order_items.order_id = 93947)  (cost=10094 rows=9958) (actual time=0.189..28.6 rows=2 loops=1)
    -> Table scan on order_items  (cost=10094 rows=99575) (actual time=0.173..25.1 rows=100000 loops=1)
```

#### 인덱스 생성

```sql
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
```

#### 인덱스 적용 후

**약 25.1ms -> 0.118ms (약 99.53% 개선)**

```text
-> Index lookup on order_items using idx_order_items_order_id (order_id=93947), with index condition: (order_items.order_id = 93947)  (cost=0.7 rows=2) (actual time=0.114..0.118 rows=2 loops=1)
```

#### 2.4.2. `product_id` 기준 조회

상품 ID(`product_id`) 기준 주문 상품 조회 쿼리

#### 인덱스 적용 전

```text
-> Filter: (order_items.product_id = 72)  (cost=10094 rows=9958) (actual time=0.556..31.6 rows=943 loops=1)
    -> Table scan on order_items  (cost=10094 rows=99575) (actual time=0.482..28 rows=100000 loops=1)
```

#### 인덱스 생성

```sql
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
```

#### 인덱스 적용 후

**약 28ms -> 4.67ms (약 83.32% 개선)**

```text
-> Index lookup on order_items using idx_order_items_product_id (product_id=72), with index condition: (order_items.product_id = 72)  (cost=330 rows=943) (actual time=0.69..4.67 rows=943 loops=1)
```