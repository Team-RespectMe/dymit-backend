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

## 통계 계산 및 갱신 기준

- 통계 조회 시 새로 완료된 회차와 과제·일정·참여·제출 변경을 확인하고, 변경이 있으면 해당 회차의 누적 통계를 갱신합니다. 개인 통계와 구성원 통계 목록도 같은 기준을 사용합니다.
- 일정은 생성 시각과 관계없이 시작 시각이 가입 시각 이후이고 조회 시각보다 이전이면 완료 일정으로 계산합니다. 일정 시작 시각에 참여 등록이 유효하면 참석으로 계산합니다.
- 과제는 마감 시각이 가입 시각 이후이고 선택한 회차 시작 시각까지 지난 경우 계산합니다. 과제 생성 시각과 할당 시각은 가입 이후 조건으로 사용하지 않으며, 회차 기준 시각에 할당이 유효해야 합니다.
- 그룹 회차는 해당 회차를 처음 계산한 그룹 통계 요청 시점의 활성 가입 관계를 저장합니다. 한 요청에서 처음 계산하는 회차가 여러 개면 모두 같은 활성 가입 관계 집합을 사용합니다.
- 저장된 그룹 회차는 이후 가입·탈퇴가 발생해도 저장한 가입 관계 집합을 유지합니다. 원천 데이터가 바뀌면 그 집합 안에서만 값을 다시 계산합니다. 개인 통계를 먼저 조회해도 그룹 회차의 가입 관계 집합은 확정되지 않습니다.
- `activeMemberCount`는 그룹 회차에 저장된 가입 관계 수가 아니라 조회 시점의 활성 사용자 수입니다.

기존 개인 통계 캐시는 계산 규칙 버전을 확인하여 조회 시 새 규칙으로 자동 재계산합니다. 기존 그룹 회차 중 가입 관계 집합이 저장되지 않은 데이터는 당시 집합을 알 수 없으므로 저장된 값과 회차 정보를 그대로 보존합니다. 이런 기존 회차는 원천 데이터 변경에 따른 재계산 대상에서 제외되며, 이후 처음 생성되는 회차부터 가입 관계 집합을 저장합니다.

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

활성 구성원 전체를 제출률과 참석률의 합계가 높은 순으로 정렬합니다. 합계가 같으면 참석률이 높은 구성원이 먼저 오며, 두 값이 모두 같으면 내부 가입 관계 순서를 안정적으로 유지합니다. `cursor`는 `_links.next.href`에서 받은 가입 관계 ID를 그대로 사용해야 하며, 형식이 잘못됐거나 현재 그룹의 활성 가입 관계가 아니면 `400 Bad Request`를 반환합니다.

각 페이지 요청은 활성 구성원 전체의 통계를 같은 조회 시각으로 갱신하고 메모리에서 정렬하므로, 활성 인원이 늘수록 조회·정렬 비용이 증가합니다. 페이지 요청 사이에 구성원의 통계가 바뀌면 순위가 이동하여 항목이 중복되거나 누락될 수 있습니다. 일관된 최신 순위가 필요하면 첫 페이지부터 다시 조회해야 합니다.

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
| `counts` | 가입 이후부터 `statisticsAt`까지의 누적 개수. 개인은 해당 가입 관계의 기간을 적용하고, 그룹은 회차를 처음 계산할 때 저장한 가입 관계별 값을 합산. 진행 회차가 없으면 모두 0 |
| `counts.assignedTaskCount` | 마감 시각이 가입 이후이고 기준 시각까지 지난 과제 중 기준 시각에 본인 할당이 유효한 과제 수. 과제 생성·할당 시각은 가입 이후 조건으로 사용하지 않으며, 기준 시각까지 과제·관련 일정·할당이 삭제된 경우 제외 |
| `counts.submittedTaskCount` | 계산 대상 과제 중 기준 시각의 상태가 제출 완료인 과제 수. 기한 내 제출 여부를 별도로 구분하지 않음 |
| `counts.eligibleScheduleCount` | 생성 시각과 관계없이 시작 시각이 가입 이후이고 선택된 회차까지 진행된 일정 수. 동일 시각은 서버 회차 순서를 적용하며 기준 시각까지 삭제된 일정 제외 |
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
