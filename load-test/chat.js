import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ROOM_COUNT = 10;
const JSON_HEADERS = { 'Content-Type': 'application/json' };

export const options = {
  stages: [
    { duration: '20s', target: 50 },
    { duration: '40s', target: 100 },
    { duration: '20s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
    'http_req_duration{name:send_message}': ['p(95)<500'],
    'http_req_duration{name:get_history}': ['p(95)<500'],
  },
};

export function setup() {
  const runId = Date.now();
  const rooms = [];
  for (let i = 0; i < ROOM_COUNT; i++) {
    const name = `load-${runId}-${i}`;
    const res = http.post(
      `${BASE_URL}/api/chatapp/chatrooms`,
      JSON.stringify({ roomName: name }),
      { headers: JSON_HEADERS, tags: { name: 'create_room' } }
    );
    check(res, { 'room created': (r) => r.status === 200 });
    rooms.push(name);
  }
  return { rooms };
}

export default function (data) {
  const room = data.rooms[(__VU + __ITER) % data.rooms.length];
  const user = `user-${__VU}`;

  if (__ITER === 0) {
    const joined = http.post(
      `${BASE_URL}/api/chatapp/chatrooms/${room}/join`,
      JSON.stringify({ participant: user }),
      { headers: JSON_HEADERS, tags: { name: 'join_room' } }
    );
    check(joined, { 'join 200': (r) => r.status === 200 });
  }

  const sent = http.post(
    `${BASE_URL}/api/chatapp/chatrooms/${room}/messages`,
    JSON.stringify({ participant: user, message: `msg ${__VU}-${__ITER}` }),
    { headers: JSON_HEADERS, tags: { name: 'send_message' } }
  );
  check(sent, { 'send 200': (r) => r.status === 200 });

  const history = http.get(
    `${BASE_URL}/api/chatapp/chatrooms/${room}/messages?limit=10`,
    { tags: { name: 'get_history' } }
  );
  check(history, {
    'history 200': (r) => r.status === 200,
    'history has messages': (r) => r.body && r.body.indexOf('messages') !== -1,
  });

  sleep(0.5 + Math.random() * 0.5);
}
