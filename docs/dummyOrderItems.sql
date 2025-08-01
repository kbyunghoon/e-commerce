DELIMITER $$

CREATE PROCEDURE InsertDummyOrderItems()
BEGIN
    DECLARE i INT DEFAULT 0;
    DECLARE v_order_id BIGINT;
    DECLARE v_product_id BIGINT;
    DECLARE v_product_name VARCHAR(255);
    DECLARE v_quantity INT;
    DECLARE v_price_per_item INT;
    DECLARE v_status VARCHAR(20);
    DECLARE v_status_rand DOUBLE;

    WHILE i < 100000 DO
        SET v_order_id = FLOOR(1 + (RAND() * 100000)); -- Random order_id between 1 and 100000
        SET v_product_id = FLOOR(1 + (RAND() * 100));  -- Random product_id between 1 and 100
        SET v_product_name = CONCAT('Dummy Product ', v_product_id);
        SET v_quantity = FLOOR(1 + (RAND() * 5)); -- Random quantity between 1 and 5
        SET v_price_per_item = FLOOR(10000 + (RAND() * 90000)); -- Random price between 10000 and 100000

        SET v_status_rand = RAND();
        IF v_status_rand < 0.7 THEN
            SET v_status = 'COMPLETED';
        ELSEIF v_status_rand < 0.9 THEN
            SET v_status = 'PENDING';
        ELSE
            SET v_status = 'CANCELLED';
        END IF;

        INSERT INTO order_items (order_id, product_id, product_name, quantity, price_per_item, status, created_at, updated_at)
        VALUES (v_order_id, v_product_id, v_product_name, v_quantity, v_price_per_item, v_status, NOW(), NOW());

        SET i = i + 1;
    END WHILE;
END $$