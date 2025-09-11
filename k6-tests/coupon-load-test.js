import http from 'k6/http';
import {check, sleep} from 'k6';
import {Counter, Rate} from 'k6/metrics';

const successfulIssues = new Rate('successful_coupon_issues');
const failedIssues = new Rate('failed_coupon_issues');
const couponSoldOut = new Counter('coupon_sold_out_errors');
const alreadyIssued = new Counter('coupon_already_issued_errors');

export const options = {
    stages: [
        {duration: '10s', target: 1000},
    ],

    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.1'],
        successful_coupon_issues: ['rate>0.01'],
    },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const COUPON_ID = __ENV.COUPON_ID || '1';

export default function () {

    const userId = Math.floor(Math.random() * 10000) + 1;

    const couponIssuePayload = JSON.stringify({
        userId: userId,
        couponId: parseInt(COUPON_ID)
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const couponResponse = http.post(
        `${BASE_URL}/api/v1/coupons/issue`,
        couponIssuePayload,
        params
    );

    check(couponResponse, {
        '성공 (201)': (r) => r.status === 201,
    });

    if (couponResponse.status === 201) {
        const responseBody = JSON.parse(couponResponse.body);
        if (responseBody.success) {
            successfulIssues.add(1);
            console.log(`✅ 사용자 ${userId} 쿠폰 발급 성공`);
        }
    } else {
        const responseBody = JSON.parse(couponResponse.body);
        failedIssues.add(1);
        const errorCode = responseBody.error?.code;

        if (errorCode === 'COUPON_SOLD_OUT') {
            couponSoldOut.add(1);
            console.log(`💸 쿠폰 소진`);
        } else if (errorCode === 'COUPON_ALREADY_ISSUED') {
            alreadyIssued.add(1);
            console.log(`🔄 중복 발급: 사용자 ${userId}`);
        } else {
            failedIssues.add(1);
        }
    }
    sleep(Math.random() * 0.1 + 0.1);
}

export function setup() {
    console.log('🚀 선착순 쿠폰 발급 부하테스트 시작 (100개 한정)');
    console.log(`📍 Target: ${BASE_URL}`);
    console.log(`🎫 Coupon ID: ${COUPON_ID}`);
    console.log('⏱️  테스트 시간: 10초');

    return {timestamp: new Date().toISOString()};
}

export function teardown(data) {
    console.log('🏁 테스트 완료');
    console.log(`📊 테스트 시간: ${data.timestamp} ~ ${new Date().toISOString()}`);
    console.log('💡 쿠폰 100개가 모두 발급되었는지 확인하세요!');
}
