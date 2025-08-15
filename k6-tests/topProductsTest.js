import http from 'k6/http';
import { sleep } from 'k6';

export let options = {
    scenarios: {
        stress_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 50 },
                { duration: '10s', target: 50 },
                { duration: '10s', target: 200 },
                { duration: '10s', target: 200 },
                { duration: '10s', target: 500 },
            ],
            gracefulStop: '5s',
        },
    },
};

function getRandomRankingDate() {
    const today = new Date();
    const pastDays = Math.floor(Math.random() * 5);
    today.setDate(today.getDate() - pastDays);
    return today.toISOString().split('T')[0];
}

export default function () {
    const randomDate = getRandomRankingDate();
    const url = `http://localhost:8080/api/v1/products/top?rankingDate=${randomDate}`;

    http.get(url);
    sleep(0.1);
}
