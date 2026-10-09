# 스터디 그룹 통계 API

## 요청 필드

| 필드 | 위치 | 의미 |
| --- | --- | --- |
| `Authorization` | 헤더 | `Bearer <ACCESS_TOKEN>`. 토큰에서 요청자의 사용자 ID와 역할을 판별 |
| `Accept` | 헤더 | 응답 형식. 예시는 `application/json` |
| `groupId` | 경로 | 조회할 그룹의 ID. 24자리 16진수 문자열 |
| `memberId` | 경로 | 조회할 사용자 ID. 24자리 16진수 문자열 |
| `size` | 목록 조회 쿼리 | 페이지 크기. 1~100의 정수이며 생략하면 20 |
| `cursor` | 목록 조회 쿼리 | 이전 응답의 `_links.next.href`에 포함된 커서 값. 생략하면 첫 페이지 |

## 요청의 암묵적 해석

| 필드 | 해석 |
| --- | --- |
| 요청자 ID | 별도로 받지 않고 `Authorization` 토큰에서 결정 |
| `latestSession` | 요청으로 받지 않고 조회 시점 이전에 시작한 유효 일정 중 시간상 최신 일정의 회차로 결정 |
| `previousSession` | 최신 일정 바로 이전에 진행된 유효 일정의 회차. 달력 주나 회차 번호에서 1을 뺀 값이 아님 |
| `statisticsAt` | 최신 진행 일정의 시작 시각. 해당 시각까지의 누적값으로 계산하며 해당 일정 참여도 포함 |
| 동일 시각의 일정 | 내부 일정 식별자 순으로 회차를 구분하며, 일정 참여 대상은 선택된 회차까지 포함 |
| `hasSchedule` | 미래 일정도 포함한 삭제되지 않은 일정의 존재 여부 |
| `size` | 생략하면 20 |
| `cursor` | 생략하면 첫 페이지. 다음 페이지는 `_links.next.href`에 포함된 값을 그대로 사용 |

## 내 관리 그룹 통계 목록

HTTP Request:

```http
GET /api/v1/study-groups/statistics?size=1 HTTP/1.1
Authorization: Bearer <ACCESS_TOKEN>
Accept: application/json
```

HTTP Response:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "traceId": "example-trace-id",
  "status": 200,
  "data": {
    "_links": {
      "next": {
        "href": "https://api.example.com/api/v1/study-groups/statistics?cursor=650000000000000000000001&size=1"
      }
    },
    "count": 1,
    "items": [
      {
        "_links": {},
        "latestSession": 8,
        "statisticsAt": "2026-10-05T10:00:00Z",
        "activeMemberCount": 8,
        "taskSubmissionRate": 75.0,
        "scheduleAttendanceRate": 70.0,
        "hasSchedule": true,
        "group": {
          "id": "650000000000000000000001",
          "name": "알고리즘 스터디"
        }
      }
    ]
  }
}
```

## 그룹 통계

HTTP Request:

```http
GET /api/v1/study-groups/650000000000000000000001/statistics HTTP/1.1
Authorization: Bearer <ACCESS_TOKEN>
Accept: application/json
```

HTTP Response:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "traceId": "example-trace-id",
  "status": 200,
  "data": {
    "_links": {},
    "counts": {
      "submittedTaskCount": 18,
      "assignedTaskCount": 24,
      "attendedScheduleCount": 14,
      "eligibleScheduleCount": 20
    },
    "taskSubmissionRate": 75.0,
    "scheduleAttendanceRate": 70.0,
    "previousTaskSubmissionRate": 60.0,
    "previousScheduleAttendanceRate": 65.0,
    "latestSession": 8,
    "statisticsAt": "2026-10-05T10:00:00Z",
    "previousSession": 7,
    "activeMemberCount": 8,
    "group": {
      "id": "650000000000000000000001",
      "name": "알고리즘 스터디"
    }
  }
}
```

## 개인 통계

HTTP Request:

```http
GET /api/v1/study-groups/650000000000000000000001/members/650000000000000000000101/statistics HTTP/1.1
Authorization: Bearer <ACCESS_TOKEN>
Accept: application/json
```

HTTP Response:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "traceId": "example-trace-id",
  "status": 200,
  "data": {
    "_links": {},
    "groupId": "650000000000000000000001",
    "memberId": "650000000000000000000101",
    "counts": {
      "submittedTaskCount": 3,
      "assignedTaskCount": 4,
      "attendedScheduleCount": 7,
      "eligibleScheduleCount": 10
    },
    "taskSubmissionRate": 75.0,
    "scheduleAttendanceRate": 70.0,
    "latestSession": 8,
    "statisticsAt": "2026-10-05T10:00:00Z"
  }
}
```

## 구성원 통계 목록

HTTP Request:

```http
GET /api/v1/study-groups/650000000000000000000001/statistics/members?size=1 HTTP/1.1
Authorization: Bearer <ACCESS_TOKEN>
Accept: application/json
```

HTTP Response:

```http
HTTP/1.1 200 OK
Content-Type: application/json
```

```json
{
  "traceId": "example-trace-id",
  "status": 200,
  "data": {
    "_links": {
      "next": {
        "href": "https://api.example.com/api/v1/study-groups/650000000000000000000001/statistics/members?cursor=650000000000000000000201&size=1"
      }
    },
    "count": 1,
    "items": [
      {
        "_links": {},
        "groupId": "650000000000000000000001",
        "memberId": "650000000000000000000101",
        "counts": {
          "submittedTaskCount": 3,
          "assignedTaskCount": 4,
          "attendedScheduleCount": 7,
          "eligibleScheduleCount": 10
        },
        "taskSubmissionRate": 75.0,
        "scheduleAttendanceRate": 70.0,
        "latestSession": 8,
        "statisticsAt": "2026-10-05T10:00:00Z"
      }
    ]
  }
}
```

## 오류 응답 예시

HTTP Request:

```http
GET /api/v1/study-groups/650000000000000000000001/statistics/members?size=101 HTTP/1.1
Authorization: Bearer <ACCESS_TOKEN>
Accept: application/json
```

HTTP Response:

```http
HTTP/1.1 400 Bad Request
Content-Type: application/json
```

```json
{
  "traceId": "example-trace-id",
  "status": 400,
  "code": "BAD_REQUEST",
  "message": "size는 1 이상 100 이하여야 합니다.",
  "errors": []
}
```

## 응답 필드

| 필드 | 의미 |
| --- | --- |
| `traceId` | 요청 추적 ID. null일 수 있음 |
| `status` | HTTP 상태 코드 |
| `data` | 성공 응답 데이터 |
| `_links` | 응답 링크 객체. 다음 페이지가 없으면 `{}` |
| `_links.next.href` | 다음 페이지 요청 URL. `cursor`와 `size`를 포함하며 마지막 페이지에서는 생략 |
| `group` | 그룹 통계 상세·관리 그룹 목록의 그룹 정보 객체 |
| `group.id` | 그룹 식별자 |
| `group.name` | 현재 그룹 이름 |
| `groupId` | 개인·구성원 통계에 포함되는 그룹 ID |
| `memberId` | 사용자 ID |
| `latestSession` | 최근 진행 일정의 회차 번호. 진행된 일정이 없으면 null |
| `previousSession` | 직전 진행 일정의 회차 번호. 이전 회차가 없으면 null. 그룹 상세에만 포함 |
| `statisticsAt` | 누적 통계 기준이 되는 최근 일정의 시작 시각, ISO 8601 UTC. 진행된 일정이 없으면 null |
| `activeMemberCount` | 현재 그룹에 속한 활성 사용자 수. 소유자·관리자를 포함하고 탈퇴자는 제외 |
| `hasSchedule` | 삭제되지 않은 일정 존재 여부. 미래 일정만 있어도 true |
| `counts` | 가입 이후부터 `statisticsAt`까지의 누적 개수. 개인은 현재 가입 기간, 그룹은 탈퇴 시점까지 보존된 이전 가입 기간을 포함한 합계. 진행 회차가 없으면 모두 0 |
| `counts.assignedTaskCount` | 가입 이후 할당되어 기준 시각까지 마감된 유효 과제 수. 기준 시각까지 과제·관련 일정·할당이 삭제된 경우 제외 |
| `counts.submittedTaskCount` | 계산 대상 과제 중 기준 시각의 상태가 제출 완료인 과제 수. 기한 내 제출 여부를 별도로 구분하지 않음 |
| `counts.eligibleScheduleCount` | 가입 이후 생성되어 선택된 회차까지 진행된 일정 수. 동일 시각은 서버 회차 순서를 적용하며 기준 시각까지 삭제된 일정 제외 |
| `counts.attendedScheduleCount` | 계산 대상 일정 중 시작 시각까지 참여 등록했고, 시작 시각에 취소되지 않았던 일정 수. 실제 체크인 대신 참여 등록을 기준으로 계산 |
| `taskSubmissionRate` | `submittedTaskCount / assignedTaskCount × 100`, 단위 %. 소수점 둘째 자리 반올림, 분모가 0이면 0.0 |
| `scheduleAttendanceRate` | `attendedScheduleCount / eligibleScheduleCount × 100`, 단위 %. 소수점 둘째 자리 반올림, 분모가 0이면 0.0 |
| `previousTaskSubmissionRate` | 직전 진행 회차 시작 시점까지의 누적 제출률. 이전 회차가 없으면 0.0. 그룹 응답에만 포함 |
| `previousScheduleAttendanceRate` | 직전 진행 회차 시작 시점까지의 누적 참석률. 이전 회차가 없으면 0.0. 그룹 응답에만 포함 |
| `count` | 현재 페이지의 항목 수 |
| `items` | 개인 통계 객체 목록 |
| `code` | 오류 코드. 오류 응답에만 포함 |
| `message` | 오류 설명. 오류 응답에만 포함 |
| `errors` | 필드별 오류 목록. 각 항목의 `field`는 필드명, `value`는 입력값, `reason`은 오류 사유 |

