DELIMITER $$

create procedure InsertDummyProductRanking()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE total_count INT DEFAULT 20000;
    DECLARE ranking_date DATE;
    DECLARE days_offset INT;

    WHILE i <= total_count DO
            -- 0~4일 전 날짜 랜덤
            SET days_offset = FLOOR(RAND() * 5);
            SET ranking_date = CURDATE() - INTERVAL days_offset DAY;

            -- INSERT
INSERT INTO product_rankings (
    product_id,
    total_sales_count,
    `rank`,
    created_at,
    ranking_date
)
VALUES (
           i,
           FLOOR(10 + (RAND() * 990)), -- 10~999 사이의 랜덤 판매량
           i,                          -- rank는 단순 증가, 필요 시 랜덤 조정 가능
           NOW(),
           ranking_date
       );

SET i = i + 1;
END WHILE;
END;

END $$