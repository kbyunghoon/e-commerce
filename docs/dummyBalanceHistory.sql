DELIMITER $$

CREATE PROCEDURE InsertDummyBalanceHistory()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_user_id BIGINT;
    DECLARE v_amount INT;
    DECLARE v_before_amount INT;
    DECLARE v_after_amount INT;
    DECLARE v_type VARCHAR(20);
    DECLARE v_transaction_at DATETIME;
    DECLARE v_type_rand DOUBLE;

    WHILE i < 100000 DO
        SET v_user_id = FLOOR(1 + (RAND() * 1000)); -- Random user_id between 1 and 1000
        SET v_amount = FLOOR(1000 + (RAND() * 99000)); -- Random amount between 1000 and 100000
        SET v_before_amount = FLOOR(100000 + (RAND() * 900000)); -- Random initial balance

        SET v_type_rand = RAND();
        IF v_type_rand < 0.4 THEN
            SET v_type = 'CHARGE';
            SET v_after_amount = v_before_amount + v_amount;
        ELSEIF v_type_rand < 0.8 THEN
            SET v_type = 'DEDUCT';
            SET v_after_amount = v_before_amount - v_amount;
            IF v_after_amount < 0 THEN SET v_after_amount = 0; END IF; -- Ensure balance doesn't go negative
        ELSE
            SET v_type = 'REFUND';
            SET v_after_amount = v_before_amount + v_amount;
        END IF;

        SET v_transaction_at = DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY); -- Transaction within the last year

        INSERT INTO balance_history (user_id, amount, before_amount, after_amount, type, transaction_at)
        VALUES (v_user_id, v_amount, v_before_amount, v_after_amount, v_type, v_transaction_at);

        SET i = i + 1;
    END WHILE;
END $$