# 종합 동시성 문제 해결 보고서

## 1. 배경

e-commerce 서비스에서는 높은 사용자 트래픽과 동시 요청 처리 요구로 인해 다양한 **동시성 문제**가 발생할 수 있다.

**포인트 시스템**, **재고 시스템**, **쿠폰 시스템**에서 발생한 대표적인 동시성 문제를 식별하고, 각 도메인 특성에 맞는 해결 전략을 도입하여 성능 개선과 데이터 정합성을 확보한 과정을 작성해 보았다.

---

## 2. 문제 정의 및 원인 분석

### 2.1 포인트 시스템

**문제 상황**

```text
// 동시성 문제 시나리오
사용자 A의 잔액: 1000원
요청 1: 500원 충전 → 1500원 예상
요청 2: 300원 충전 → 1800원 예상
실제 결과: 1300원 (Lost Update 발생)
```

* **문제**: 동시 충전 요청 시 잔액이 올바르게 갱신되지 않음 (Lost Update)
* **원인**: 동일한 사용자 balance 필드를 두 트랜잭션이 동시에 갱신하면서 Race Condition 발생

### 2.2 재고 시스템

**문제 상황**

```text
// 동시성 문제 시나리오  
상품 A의 재고: 5개
주문 1: 3개 주문 → 2개 남음 예상
주문 2: 4개 주문 → 1개 남음 예상
실제 결과: 1개
```

* **문제**: 동시 주문 시 재고를 초과하여 판매됨 (Over-selling)
* **원인**: 재고 확인과 차감이 원자적으로 처리되지 않아 Phantom Read 및 Race Condition 발생

### 2.3 쿠폰 시스템

**문제 상황**

```text
// 동시성 문제 시나리오
한정 쿠폰 수량: 100개
동시 발급 요청: 1000명
예상 결과: 100명 성공, 900명 실패
실제 결과: 150명 성공 (Over-Issue 발생)
```

* **문제**: 한정 쿠폰을 초과 발급하거나 동일 사용자가 중복 발급됨 (Over-Issue)
* **원인**: 수량 확인과 발급 사이 비원자적 처리 / 중복 검증 누락

---

## 3. 해결 전략

### 3.1 전략 비교

| 구분     | 낙관적 락 + Retry | 비관적 락  |
|--------|---------------|--------|
| 충돌 처리  | 재시도           | 자동 대기  |
| 성능     | 높음            | 낮음     |
| 동시성    | 우수            | 제한적    |
| 정합성    | 재시도로 확보       | 즉시 확보  |
| 적합한 상황 | 낮은 충돌률        | 높은 충돌률 |

### 3.2 도메인별 적용 전략

* **포인트**: 낙관적 락 + Spring Retry (충돌 빈도 낮고 성능 우선)
* **재고**: 비관적 락 (재고 무결성 중요, 충돌 잦음)
* **쿠폰**: 비관적 락 (정확한 수량 관리 필수, 동시 요청 다수)

---

## 4. 상세 구현 방법

### 4.1 포인트 시스템

* **기술 스택**: JPA `@Version`, Spring Retry

```kotlin
@Entity
@Table(name = "users")
class UserEntity(
    @Version
    val version: Long = 0
)

class BalanceService {
    @Retryable(
        value = [ObjectOptimisticLockingFailureException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 200)
    )
    @Transactional
    fun charge(command: BalanceChargeCommand): BalanceInfo {
        val user = userRepository.findByIdOrThrow(command.userId)
        user.chargeBalance(command.amount)
        return BalanceInfo.from(userRepository.save(user))
    }
}
```

### 4.2 재고 시스템

* **기술 스택**: JPA Pessimistic Lock (`PESSIMISTIC_WRITE`)
* **핵심 구현**: 비관적 락 적용

```kotlin
@Repository
interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") productId: Long): ProductEntity?
}
```

### 4.3 쿠폰 시스템

* **기술 스택**: JPA Pessimistic Lock (`PESSIMISTIC_WRITE`), 중복 발급 체크
* **핵심 구현**: 비관적 락 적용

```kotlin
@Repository
interface CouponJpaRepository : JpaRepository<CouponEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id = :id")
    fun findByIdWithPessimisticLock(@Param("id") productId: Long): CouponEntity?
}

class CouponService {
    @Transactional
    fun issue(command: CouponIssueCommand): UserCouponInfo {
        val coupon = couponRepository.findByIdWithPessimisticLock(command.couponId)

        if (!coupon.canBeIssued()) throw BusinessException(ErrorCode.COUPON_SOLD_OUT)
        if (userCouponRepository.existsByUserIdAndCouponId(command.userId, command.couponId))
            throw BusinessException(ErrorCode.COUPON_ALREADY_ISSUED)

        coupon.issue()
        return null // 발급 처리
    }
}
```

---

## 5. 실험 및 성능 테스트 결과

### 5.1 포인트 시스템 (Spring Retry 적용 전후 비교)

| 테스트 시나리오             | 적용 전 | 예상 성공률 | 적용 후 | 개선율       |
|----------------------|------|--------|------|-----------|
| 중간 부하 (5스레드, 10작업)   | 40%  | 90%    | 90%  | **+125%** |
| 대량 부하 (50스레드, 100작업) | 38%  | N/A    | 85%  | **+124%** |

### 5.2 재고 시스템

| 테스트 시나리오               | 예상 성공률 | 실제 성공률 | 데이터 정합성    |
|------------------------|--------|--------|------------|
| 기본 재고 차감 (10스레드, 20작업) | 100%   | 100%   | ✅ 완전 보장    |
| 높은 동시성 (20스레드, 100작업)  | 50%    | 50%    | ✅ 완전 보장    |
| 재고 부족 상황 (10스레드, 20작업) | 25%    | 25%    | ✅ 음수 재고 방지 |

* 테스트: 20스레드 100주문
* 결과: 성공률 50%, **재고 음수 없음 → 정합성 100% 보장**

### 5.3 쿠폰 시스템

| 테스트 시나리오             | 예상 성공률 | 실제 성공률 | 데이터 정합성  |
|----------------------|--------|--------|----------|
| 일반 발급 (50스레드, 100작업) | 10%    | 10%    | ✅ 정확한 수량 |
| 중복 방지 (10스레드, 20작업)  | 5%     | 5%     | ✅ 1회만 발급 |
| 단일 쿠폰 (50스레드, 100작업) | 1%     | 1%     | ✅ 1명만 성공 |

* 테스트: 100개 발급 가능한 쿠폰 1000명 동시 발급 시도
* 결과: 정확히 100명 발급 성공 → **Over-Issue 방지 성공**

---

## 6. 한계점 및 보완 방안

### 6.1 낙관적 락의 한계

* 재시도 실패 가능성 존재
* 응답 지연 발생 가능

**보완책**:

* 적응형 백오프 / 서킷 브레이커 / 배치 처리 도입

### 6.2 비관적 락의 한계

* TPS 저하, 데드락 가능성

**보완책**:

* ID 정렬로 데드락 방지 (상품 관련 결제 시 상품 ID 정렬 완료하여 데드락 방지 완료)
* 분산 락 및 큐 기반 처리 고려

---

## 7. 향후 개선 방향

* **단기**: 재시도 지표 모니터링, 백오프 튜닝
* **중기**: Redis 락, 이벤트 소싱
* **장기**: 비동기 큐 기반 설계로 확장