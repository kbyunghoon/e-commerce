-- 선착순 쿠폰 부하테스트용 더미 데이터
TRUNCATE coupons
-- 1. 쿠폰 데이터 생성
INSERT INTO coupons (name, code, discount_type, discount_value, expires_at, total_quantity, issued_quantity, created_at, updated_at)
VALUES
('선착순 10% 할인쿠폰', 'LOAD_TEST_COUPON', 'PERCENTAGE', 10, DATE_ADD(NOW(), INTERVAL 30 DAY), 100, 0, NOW(), NOW());

-- 2. 사용자 데이터 생성 (1~10000명)
-- CROSS JOIN을 이용한 숫자 생성 테이블 방식
INSERT INTO users (balance, name, email, created_at, updated_at, version)
SELECT
    10000 as balance,
    CONCAT('TestUser', n) as name,
    CONCAT('testuser', n, '@test.com') as email,
    NOW() as created_at,
    NOW() as updated_at,
    0 as version
FROM (
    SELECT
        t1.d * 1000 + t2.d * 100 + t3.d * 10 + t4.d + 1 as n
    FROM
        (SELECT 0 as d UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t1
        CROSS JOIN
        (SELECT 0 as d UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t2
        CROSS JOIN
        (SELECT 0 as d UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t3
        CROSS JOIN
        (SELECT 0 as d UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) t4
) numbers
WHERE n <= 10000
ORDER BY n;

-- 상품 더미데이터
INSERT INTO products (name, price, stock, status, created_at, updated_at) VALUES
('맥북 프로', 1500000, 100, 'ACTIVE', NOW(), NOW()),
('애플 키보드', 120000, 50, 'ACTIVE', NOW(), NOW()),
('트랙패드', 50000, 200, 'ACTIVE', NOW(), NOW()),
('C타입 허브', 30000, 150, 'ACTIVE', NOW(), NOW()),
('SSD 1TB', 180000, 75, 'ACTIVE', NOW(), NOW());

-- 상품 랭킹 더미데이터
INSERT INTO product_rankings (product_id, total_sales_count, `rank`, created_at, ranking_date) VALUES
(1, 100, 1, NOW(), NOW()), -- 맥북 프로
(2, 80, 2, NOW(), NOW()),  -- 애플 키보드
(3, 60, 3, NOW(), NOW());  -- 트랙패드
