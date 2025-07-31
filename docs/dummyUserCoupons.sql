DELIMITER $$

CREATE PROCEDURE InsertDummyUserCoupons()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_user_id BIGINT;
    DECLARE v_coupon_id BIGINT;
    DECLARE v_status VARCHAR(20);
    DECLARE v_issued_at DATETIME;
    DECLARE v_used_at DATETIME;

    WHILE i < 100000 DO
        SET v_user_id = FLOOR(1 + (RAND() * 1000)); -- Random user_id between 1 and 1000
        SET v_coupon_id = FLOOR(1 + (RAND() * 50));  -- Random coupon_id between 1 and 50

        SET v_issued_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY); -- Issued within the last year

        SET v_status = CASE
            WHEN RAND() < 0.6 THEN 'AVAILABLE'
            WHEN RAND() < 0.9 THEN 'USED'
            ELSE 'EXPIRED'
        END;

        IF v_status = 'USED' THEN
            SET v_used_at = DATE_ADD(v_issued_at, INTERVAL FLOOR(RAND() * 30) DAY); -- Used within 30 days of issue
            IF v_used_at > NOW() THEN SET v_used_at = NOW(); END IF; -- Ensure used_at is not in the future
        ELSE
            SET v_used_at = NULL;
        END IF;

        INSERT INTO user_coupons (user_id, coupon_id, status, issued_at, used_at)
        VALUES (v_user_id, v_coupon_id, v_status, v_issued_at, v_used_at);

        SET i = i + 1;
    END WHILE;
END $$