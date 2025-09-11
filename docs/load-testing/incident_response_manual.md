# 선착순 쿠폰 시스템 가상 장애 대응 매뉴얼

## 1. 개요

### 1.1 목적
선착순 쿠폰 발급 시스템에서 발생할 수 있는 가상 장애 상황에 대한 대응 방안

### 1.2 적용 범위
- Redis 기반 쿠폰 발급 검증 시스템
- Kafka 기반 비동기 이벤트 처리
- MySQL 기반 쿠폰 데이터 영속화

## 2. 장애 분류 및 대응 우선순위

### 2.1 Critical (P0) - 5분 내 대응
- 🚨 **전체 서비스 다운**: API 응답 불가
- 🚨 **쿠폰 중복 발급**: 데이터 무결성 문제
- 🚨 **대량 에러 발생**: 에러율 > 50%

### 2.2 High (P1) - 15분 내 대응  
- ⚠️ **성능 심각 저하**: 응답 시간 > 5초
- ⚠️ **Kafka Consumer 장애**: 이벤트 처리 중단
- ⚠️ **Redis 연결 장애**: 쿠폰 발급 불가

### 2.3 Medium (P2) - 1시간 내 대응
- 🟡 **DB 연결 풀 고갈**: 간헐적 장애
- 🟡 **메모리 누수**: 점진적 성능 저하
- 🟡 **로그 급증**: 디스크 용량 부족

## 3. 주요 장애 시나리오별 대응 방안

### 3.1 🚨 P0: 전체 서비스 다운

#### 증상
- HTTP 500/503 에러 급증
- 모든 쿠폰 발급 요청 실패
- Health Check 실패

#### 근본 원인 분석
- **DB 연결 실패**: 네트워크/DB 상태 확인
- **Redis 장애**: Redis 서버 상태 점검

#### 복구 절차
1. **임시 조치**: Circuit Breaker 활성화
2. **서비스 재시작**: 단계적 트래픽 복구

---

### 3.2 🚨 P0: 쿠폰 중복 발급 (데이터 무결성 문제)

#### 증상
- 동일 사용자가 같은 쿠폰 여러 개 보유
- 설정된 쿠폰 수량 초과 발급
- Redis와 DB 데이터 불일치

#### 즉시 대응 (5분 내)
1. 긴급 쿠폰 발급 중단
```bash
redis-cli SET coupon:stock:1 0
```

2. 중복 발급 현황 확인
```sql
SELECT userId, COUNT(*) as cnt 
  FROM user_coupon 
  WHERE couponId = 1 
  GROUP BY userId 
  HAVING cnt > 1;
```

#### 데이터 정합성 복구
```sql
-- 1. 중복 발급 데이터 식별
WITH duplicates AS (
  SELECT userId, couponId, 
         ROW_NUMBER() OVER (PARTITION BY userId, couponId ORDER BY issuedAt) as rn
  FROM user_coupon 
  WHERE couponId = 1
)
-- 2. 첫 번째 이외 모든 중복 데이터 삭제
DELETE FROM user_coupon 
WHERE id IN (
  SELECT id FROM duplicates WHERE rn > 1
);

-- 3. 쿠폰 발급 수량 정정
UPDATE coupon 
SET issuedQuantity = (
  SELECT COUNT(*) FROM user_coupon WHERE couponId = 1
) 
WHERE id = 1;
```

#### 예방 조치
- Redis Lua Script 로직 재검토
- 모니터링 알람 강화

---

### 3.3 ⚠️ P1: Redis 연결 장애

#### 증상
- 모든 쿠폰 발급 요청 실패
- Redis 명령어 응답 없음

#### 즉시 대응 (15분 내)
```bash
# 1. Redis 서버 상태 확인
redis-cli ping

# 2. Redis 연결 수 확인
redis-cli info clients
```

#### 임시 우회 방안
```kotlin
// Fallback 로직 추가
@Service
class CouponService {
    fun issueWithFallback(command: CouponIssueCommand) {
        try {
            // Redis 기반 정상 플로우
            issue(command)
        } catch (e: RedisConnectionException) {
            // DB 기반 Fallback (성능 저하 허용)
            issueDirectlyFromDB(command)
        }
    }
}
```

---

### 3.5 🟡 P2: DB 연결 풀 고갈

#### 증상
- `HikariPool connection timeout` 로그
- 간헐적 503 Service Unavailable
- DB 응답 시간 급증

#### 대응 방안
```yaml
# 연결 풀 설정 조정
spring:
  datasource:
    hikari:
      maximum-pool-size: 30        # 증설
      connection-timeout: 30000    # 타임아웃 연장
      leak-detection-threshold: 60000
```

## 4. 모니터링 및 알람 기준

### 4.1 Critical 알람 (즉시 발송)
- **에러율** > 10%
- **응답시간 P99** > 5초
- **DB 연결 풀 사용률** > 90%
- **Kafka Consumer Lag** > 10000

### 4.2 Warning 알람 (5분 지연)
- **에러율** > 1%
- **응답시간 P95** > 1초
- **Redis 응답시간** > 100ms
- **JVM Heap 사용률** > 80%

### 4.3 주요 메트릭 수집
```yaml
# Prometheus 메트릭
- http_requests_total{status!="201"}
- http_request_duration_seconds{quantile="0.95"}
- hikaricp_connections_active
- kafka_consumer_lag_sum
- redis_connected_clients
- jvm_memory_used_bytes
```

## 5. 장애 대응 체크리스트

### 5.1 장애 발생 시 (1차 대응)
- [ ] 장애 접수 및 우선순위 결정
- [ ] 관련팀 알림 (Slack, SMS)
- [ ] 모니터링 대시보드 확인
- [ ] 서비스 상태 점검
- [ ] 임시 조치 실행

### 5.2 원인 분석 (2차 대응)
- [ ] 로그 분석 (ERROR, WARN 레벨)
- [ ] 시스템 리소스 확인
- [ ] 외부 의존성 상태 점검
- [ ] 데이터 정합성 검증
- [ ] 근본 원인 식별

### 5.3 복구 완료 (3차 대응)
- [ ] 서비스 정상화 확인
- [ ] 모니터링 지표 안정화
- [ ] 데이터 정합성 재검증
- [ ] 장애 보고서 작성
- [ ] 재발 방지 계획 수립