#!/bin/bash
KAFKA_HOST="kafka-1:29092,kafka-2:29094,kafka-3:29096"

KAFKA_CLI_PREFIX="docker compose -f docker-compose-kafka.yml exec kafka-1"

echo "프로듀서 연습"
echo "1. 기본 메시지 발행"
echo "2. 키-값 메시지 발행"
echo "3. JSON 메시지 발행"

read -p "번호 선택 (1-3): " choice

case $choice in
    1)
        echo ""
        echo "기본 메시지 발행"
        echo "=================================================="
        echo "메세지 입력"
        echo ""

        $KAFKA_CLI_PREFIX kafka-console-producer \
            --bootstrap-server $KAFKA_HOST \
            --topic test-topic
        ;;

    2)
        echo ""
        echo "🔑 키-값 메시지 발행"
        echo "=================================================="
        echo "key:value 형태로 입력"
        echo "예: user123:안녕하세요"
        echo ""

        $KAFKA_CLI_PREFIX kafka-console-producer \
            --bootstrap-server $KAFKA_HOST \
            --topic test-topic \
            --property "parse.key=true" \
            --property "key.separator=:"
        ;;
        
    3)
        echo ""
        echo "JSON 메시지 발행"
        echo "=================================================="
        
        messages=(
            '{"userId":1001,"action":"login","timestamp":"2025-09-04T10:30:00Z"}'
        )
        
        for message in "${messages[@]}"; do
            echo "발행: $message"
            echo "$message" | $KAFKA_CLI_PREFIX kafka-console-producer \
                --bootstrap-server $KAFKA_HOST \
                --topic test-topic
            sleep 1
        done
        
        echo "JSON 메시지 발행 완료"
        ;;
        
    *)
        echo ""
        exit 1
        ;;
esac

echo ""
