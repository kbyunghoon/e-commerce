# e-commerce 서비스

## 클린 아키텍쳐 개요

해당 프로젝트는 클린 아키텍쳐 패턴을 기반으로 설계되었습니다.

### 클린 아키텍처 선택 이유

- 비즈니스 로직이 프레임워크나 외부 기술로부터 독립적으로 유지되므로, 기술 스택 변경이나 업그레이드 시 핵심 로직에 미치는 영향을 최소화할 수 있기 때문에 유지보수성이 향상됩니다.
- 각 레이어가 명확히 분리되어 있어, 비즈니스 로직을 외부 의존성 없이 순수하게 단위 테스트할 수 있기 때문에 테스트에 용이합니다.
- 새로운 기능 추가나 기존 기능 변경 시, 특정 레이어의 변경이 다른 레이어에 미치는 효과를 줄여주기 때문에 유연한 확장이 가능합니다.
- 각 레이어의 역할과 책임이 명확하게 정의되어 있어, 개발자가 코드의 특정 부분을 이해하고 수정하는 데 드는 시간을 줄여 협업 효율성을 높이고 코드 품질을 유지가 가능합니다.

### 레이어 간 의존성 방향

클린 아키텍쳐의 핵심 원칙은 **의존성 규칙(Dependency Rule)** 입니다.
외부 레이어는 내부 레이어에 의존할 수 있지만, 내부 레이어는 외부 레이어에 의존할 수 없습니다.
의존성의 방향은 항상 안쪽으로 향합니다.

```
+-------------------------+
|      Presentation       |
|   (Controllers, DTOs)   |
+------------^------------+
             |
+------------+------------+
|      Application        |
|       (Services)        |
+------------^------------+
             |
+------------+------------+
|         Domain          |
|   (Entities, Business   |
|      Rules)             |
+------------^------------+
             |
+------------+------------+
|     Infrastructure      |
|  (Persistence, Adapters)|
+-------------------------+
```

### 각 레이어별 책임 및 제약사항

#### 1. Domain Layer (도메인 계층)
- **책임**: 핵심 비즈니스 로직과 엔티티를 포함합니다. 시스템의 가장 중요한 부분이며, 어떤 프레임워크나 데이터베이스에도 의존하지 않습니다.
- **제약사항**:
    - 다른 어떤 레이어에도 의존하지 않습니다.
    - 순수한 비즈니스 규칙과 엔티티만 포함합니다.

#### 2. Application Layer (애플리케이션 계층)
- **책임**: 도메인 계층의 엔티티를 사용하여 비즈니스 로직을 구현합니다.
- **제약사항**:
    - 도메인 계층에만 의존합니다.
    - 프레임워크나 외부 라이브러리에 직접적으로 의존하지 않습니다.

#### 3. Presentation Layer (프레젠테이션 계층)
- **책임**: API 엔드포인트를 담당합니다. 사용자 요청을 받아 애플리케이션 계층의 비즈니스 로직을 호출하고, 결과를 사용자에게 반환합니다.
- **제약사항**:
    - 애플리케이션 계층에만 의존합니다.
    - `Controller` 및 `DTO`를 포함합니다.

#### 4. Infrastructure Layer (인프라스트럭처 계층)
- **책임**: 외부 시스템과의 모든 상호작용을 담당합니다. 데이터베이스, 외부 기술에 대한 구체적인 구현을 포함합니다. 도메인 계층에서 정의한 Repository 인터페이스를 구현합니다.
- **제약사항**:
    - 도메인 계층에서 정의한 Repository 인터페이스를 구현합니다.
    - 애플리케이션 계층이나 프레젠테이션 계층에 직접적으로 의존하지 않습니다.

### 트랜잭션 경계 정책

트랜잭션은 애플리케이션 계층의 `Service`에서 관리됩니다.
- `@Transactional` 어노테이션은 `Service` 메서드에서만 적용합니다.
- 인프라스트럭처 계층의 Repository 구현체는 트랜잭션 관리의 책임을 가지지 않습니다.

### 패키지 구조 및 네이밍 컨벤션

```
src/main/kotlin/kr/hhplus/be/
├───application/
│   ├───balance/        // 잔액 관련 Command 및 DTO (BalanceCommand.kt, BalanceDto.kt)
│   ├───coupon/         // 쿠폰 관련 Command 및 DTO (CouponCommand.kt, CouponDto.kt)
│   ├───order/          // 주문 관련 Command 및 DTO (OrderCommand.kt, OrderDto.kt)
│   ├───product/        // 상품 관련 DTO (ProductDto.kt)
│   └───service/        // 핵심 비즈니스 로직
├───config/             // 애플리케이션 설정 (DB, Swagger 등)
├───domain/
│   ├───coupon/         // 쿠폰 도메인 엔티티, Repository 인터페이스, 관련 Enum
│   ├───exception/      // 비즈니스 예외 및 에러 코드 정의
│   ├───order/          // 주문 도메인 엔티티, Repository 인터페이스, 관련 Enum
│   └───product/        // 상품 도메인 엔티티, Repository 인터페이스, 관련 Enum
│   └───user/           // 사용자 도메인 엔티티, Repository 인터페이스, 관련 Enum
├───infrastructure/
│   ├───entity/         // JPA 엔티티 (DB 테이블 매핑)
│   └───persistence/    // Repository 인터페이스 구현체 (JPA Repository)
│       └───repository/ // Spring Data JPA Repository 인터페이스
└───presentation/
    ├───api/            // REST API 인터페이스 정의 (OpenAPI/Swagger)
    ├───controller/     // REST API 컨트롤러 구현체
    └───dto/            // 요청/응답 DTO
        ├───common/     // 공통 DTO (BaseResponse, ErrorResponse 등)
        ├───request/    // 요청 DTO
        └───response/   // 응답 DTO
```

**네이밍 컨벤션**:
- **클래스**: PascalCase (예: `OrderService`, `ProductRepositoryImpl`)
- **인터페이스**: PascalCase (예: `OrderRepository`, `ProductService`)
- **메서드**: camelCase (예: `processOrder`, `deductStock`)
- **변수**: camelCase (예: `userId`, `orderItems`)
- **패키지**: kebab-case (예: `kr.hhplus.be.application.service`)
- **DTO**: 접미사 `Dto`를 가진 클래스 (예: `BalanceDto`, `CouponDto`, `OrderDto`, `ProductDto`). 이 클래스 내부에 `Info` 접미사를 가진 중첩 데이터 클래스(예: `BalanceDto.BalanceInfo`, `ProductDto.ProductInfo`, `OrderDto.CalculatedOrderDetails`, `OrderDto.OrderCreateDto`)를 포함하여 실제 데이터를 표현합니다.
- **Command**: 접미사 `Command`를 가진 최상위 데이터 클래스 (예: `BalanceChargeCommand`, `OrderCreateCommand`). 특정 작업을 수행하기 위한 요청 데이터를 캡슐화합니다.
- **Entity**: 접미사 `Entity` (예: `ProductEntity`, `UserEntity`)
- **Repository 구현체**: 접미사 `Impl` (예: `ProductRepositoryImpl`)
- **JPA Repository 인터페이스**: 접미사 `JpaRepository` (예: `ProductJpaRepository`)
