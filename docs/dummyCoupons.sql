DELIMITER $$

CREATE PROCEDURE InsertDummyCoupons()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_name VARCHAR(255);
    DECLARE v_code VARCHAR(255);
    DECLARE v_discount_type VARCHAR(20);
    DECLARE v_discount_value INT;
    DECLARE v_expires_at DATETIME;
    DECLARE v_total_quantity INT;
    DECLARE v_issued_quantity INT;

    WHILE i < 50 DO -- Create 50 dummy coupons
        SET v_name = CONCAT('Dummy Coupon ', i + 1);
        SET v_code = CONCAT('CODE', LPAD(i + 1, 3, '0'), UUID_SHORT()); -- Unique code

        SET v_discount_type = CASE WHEN RAND() < 0.5 THEN 'PERCENTAGE' ELSE 'FIXED' END;
        IF v_discount_type = 'PERCENTAGE' THEN
            SET v_discount_value = FLOOR(10 + (RAND() * 40)); -- 10-50%
        ELSE
            SET v_discount_value = FLOOR(1000 + (RAND() * 9000)); -- 1000-10000 fixed amount
        END IF;

        SET v_expires_at = DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 365) DAY); -- Expires within the next year
        SET v_total_quantity = FLOOR(100 + (RAND() * 900)); -- 100-1000 total quantity
        SET v_issued_quantity = FLOOR(RAND() * v_total_quantity); -- Issued quantity less than total

        INSERT INTO coupons (name, code, discount_type, discount_value, expires_at, total_quantity, issued_quantity, created_at, updated_at)
        VALUES (v_name, v_code, v_discount_type, v_discount_value, v_expires_at, v_total_quantity, v_issued_quantity, NOW(), NOW());

        SET i = i + 1;
    END WHILE;
END $$