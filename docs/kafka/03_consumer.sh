#!/bin/bash
KAFKA_HOST="kafka-1:29092,kafka-2:29094,kafka-3:29096"

KAFKA_CLI_PREFIX="docker compose -f docker-compose-kafka.yml exec kafka-1"

echo "Kafka CLI 컨슈머 연습"
echo "=================================================="

# 메뉴 선택
echo "컨슈머 연습"
echo "1. 기본 메시지 소비 (처음부터)"
echo "2. 기본 메시지 소비 (최신부터)"
echo "3. 키-값 메시지 소비"
echo "4. 컨슈머 그룹으로 소비"

read -p "번호 선택: " choice

case $choice in
    1)
        echo ""
        echo "기본 메시지 소비 (처음부터)"
        echo "=================================================="
        echo "처음부터 모든 메시지를 소비"
        echo ""

        $KAFKA_CLI_PREFIX kafka-console-consumer \
            --bootstrap-server $KAFKA_HOST \
            --topic test-topic \
            --from-beginning
        ;;

    2)
        echo ""
        echo "기본 메시지 소비 (최신부터)"
        echo "=================================================="
        echo "최신 메시지부터 소비"
        echo ""

        $KAFKA_CLI_PREFIX kafka-console-consumer \
            --bootstrap-server $KAFKA_HOST \
            --topic test-topic
        ;;

    3)
        echo ""
        echo "키-값 메시지 소비"
        echo "=================================================="
        echo "키와 값 모두 표시"
        echo ""

        $KAFKA_CLI_PREFIX kafka-console-consumer \
            --bootstrap-server $KAFKA_HOST \
            --topic test-topic \
            --from-beginning \
            --property print.key=true \
            --property key.separator=" => "
        ;;

    4)
        echo ""
        echo "컨슈머 그룹으로 소비"
        echo ""

        $KAFKA_CLI_PREFIX kafka-console-consumer \
            --bootstrap-server $KAFKA_HOST \
            --topic test-topic \
            --group test-topic \
            --from-beginning
        ;;
        
    *)
        echo ""
        exit 1
        ;;
esac

echo ""
