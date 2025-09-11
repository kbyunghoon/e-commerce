#!/bin/bash
KAFKA_HOST="kafka-1:29092,kafka-2:29094,kafka-3:29096"

KAFKA_CLI_PREFIX="docker compose -f docker-compose-kafka.yml exec kafka-1"

echo "1. 기본 토픽 생성 (파티션 1개, 복제본 1개)"
$KAFKA_CLI_PREFIX kafka-topics --create \
  --bootstrap-server $KAFKA_HOST \
  --topic test-topic \
  --partitions 1 \
  --replication-factor 1

echo ""

echo "2. DLQ 토픽 생성"
$KAFKA_CLI_PREFIX kafka-topics --create \
  --bootstrap-server $KAFKA_HOST \
  --topic test-dlq \
  --partitions 1 \
  --replication-factor 3

echo ""

echo "생성된 토픽 목록 확인"
echo "=================================================="
$KAFKA_CLI_PREFIX kafka-topics --list \
  --bootstrap-server $KAFKA_HOST

echo ""

echo "기본 토픽 상세 정보"
echo "=================================================="
$KAFKA_CLI_PREFIX kafka-topics --describe \
  --bootstrap-server $KAFKA_HOST \
  --topic test-topic