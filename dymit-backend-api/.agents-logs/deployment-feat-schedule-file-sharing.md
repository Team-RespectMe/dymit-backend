# 배포 로그

TASK2 완료.
스터디 일정 첨부파일 API `GET/PUT /api/v1/study-schedules/{scheduleId}/attachments` 가 추가되었습니다.
첨부는 `schedule_attachments` 컬렉션으로 관리되며, 일정 상세 응답의 `_links.attachments` 로 접근할 수 있습니다.
파일 상태는 다중 일정 연결을 고려해 갱신되며, 첨부 교체는 Mongo 트랜잭션 경계 안에서 처리됩니다.
