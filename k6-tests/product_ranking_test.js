import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
    stages: [
        {duration: '10s', target: 1000},
    ],
}

export default function main() {
    // 테스트할 API 엔드포인트
    const url = 'http://localhost:8080/api/v1/products/top';

    // GET 요청
    let res = http.get(url);

    // 응답 검증
    check(res, {
        'status is 200': (r) => r.status === 200,
        'body is not empty': (r) => r.body && r.body.length > 0,
    });

    // 1초 대기 (사용자 행동 시뮬레이션)
    sleep(0.1);
}
