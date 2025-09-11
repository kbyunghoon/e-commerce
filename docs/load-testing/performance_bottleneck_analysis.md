# 선착순 쿠폰 시스템 성능 병목점 분석 문서

## 1. 시스템 아키텍처 분석

### 1.1 현재 쿠폰 발급 플로우
```
Client → CouponService.issue() → CouponRedisRepository.issueRequest() → Kafka Event → CouponIssueEventListener → DB Transaction
```

### 1.2 핵심 컴포넌트
- **Redis**: Lua Script로 중복 검사 + 재고 관리 (원자적 처리)
- **Kafka**: 비동기 이벤트 처리 (concurrency = 3)
- **MySQL**: 최종 쿠폰 발급 데이터 저장

## 2. 식별된 성능 병목점

### 2.1 Database Connection Pool
**위치**: `application.yml:10`
```yaml
hikari:
  maximum-pool-size: 3  # 매우 적음
  connection-timeout: 10000
  max-lifetime: 60000
```

**문제점**:
- 대용량 트래픽 시 DB 연결 부족으로 인한 대기 시간 증가
- Kafka Consumer 3개 + 기타 DB 작업으로 연결 풀 경쟁 발생

**영향도**: **HIGH** - 시스템 전체 처리량 제한

---

### 2.2 Kafka Consumer 동시성 제한
**위치**: `CouponIssueEventListener.kt:17`
```kotlin
@KafkaListener(topics = [COUPON_ISSUE_TOPIC], groupId = "coupon_issue", concurrency = "3")
```

**문제점**:
- 동시 처리 가능한 이벤트 수가 3개로 제한
- 대량 요청 시 Kafka 메시지 처리 지연 발생
- Consumer Lag 증가로 인한 쿠폰 발급 완료 지연

**영향도**: **HIGH** - 비동기 처리 병목

---

## 3. Redis Lua Script 성능 분석

### 3.1 현재 구현 (`CouponRedisRepositoryImpl.kt:19-47`)
```lua
-- 1. 중복 발급 체크 (SADD)
if redis.call('SADD', issuedKey, userId) == 0 then
    return 'ALREADY_ISSUED'
end

-- 2. 재고 확인 및 차감
local currentStock = redis.call('GET', stockKey)
if not currentStock or tonumber(currentStock) <= 0 then
    redis.call('SREM', issuedKey, userId)
    return 'SOLD_OUT'
end

-- 3. 재고 차감
local newStock = redis.call('DECR', stockKey)
if newStock < 0 then
    redis.call('INCR', stockKey)
    redis.call('SREM', issuedKey, userId)
    return 'SOLD_OUT'
end
```

- 원자적 처리로 동시성 문제 해결
- 적절한 롤백 로직 포함
- 성능상 큰 문제점 없음

## 4. 예상 성능 시나리오

### 4.1 현재 설정에서 예상 처리량
- **DB 연결 풀**: 3개 → 초당 최대 약 300 트랜잭션
- **Kafka Consumer**: 3개 → 초당 약 300-900 이벤트 처리
- **병목점**: DB 연결 풀이 가장 큰 제약

### 4.2 대용량 트래픽 시 예상 문제점
1. **1000명 동시 요청 시**:
   - Redis 처리: 정상 (수 ms)
   - Kafka 이벤트 발행: 정상
   - **DB 처리**: 병목 발생 (연결 대기)

2. **5000명 동시 요청 시**:
   - **심각한 DB 연결 부족**
   - Kafka Consumer Lag 급증
   - 시스템 응답 불가 상황 가능

## 5. 모니터링 포인트

### 5.1 핵심 지표
- **DB 연결 풀 사용률**: > 80% 시 경고
- **Kafka Consumer Lag**: > 1000 메시지 시 경고
- **Redis 응답 시간**: > 10ms 시 경고
- **JVM GC Time**: > 100ms 시 경고

### 5.2 장애 징후
- Connection timeout 에러 급증
- Kafka rebalancing 빈발
- Redis connection refused
- OutOfMemoryError 발생

## 6. 즉시 개선 권장사항 (우선순위별)

### 1순위 - DB 연결 풀 증대
```yaml
hikari:
  maximum-pool-size: 20  # 3 → 20
  minimum-idle: 10
  connection-timeout: 30000
```

### 2순위 - Kafka Consumer 동시성 증대
```kotlin
@KafkaListener(concurrency = "10")  # 3 → 10
```

### 3순위 - Redis 연결 최적화
```yaml
redis:
  jedis:
    pool:
      max-active: 20
      max-idle: 10
      min-idle: 5
```