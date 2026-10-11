# BACKLOG
---
## HOTFIX-107 통계 API 오류 수정
상태: Done

## OBJECTIVE

스터디 그룹 지표 조회의 오류 원인을 파악하십시오

URI: /api/v1/study-groups/statistics
HTTP METHOD: GET
```json
{
   "traceId":"6acb31e1882a993fd5add21416ae3435",
   "status":500,"code":"INTERNAL_SERVER_ERROR",
   "message":"Failed to instantiate net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule using constructor fun <init>(org.bson.types.ObjectId?, org.bson.types.ObjectId, kotlin.String, kotlin.String, net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleLocation, kotlin.Long, java.time.Instant, kotlin.collections.MutableSet<net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleRole>, kotlin.Long, java.time.Instant?, java.time.Instant?, kotlin.Boolean, java.time.Instant?): net.noti_me.dymit.dymit_backend_api.study_schedule.domain.StudySchedule with arguments 6905dafd81aa60d7a035a3cc,null,null,null,null,0,null,null,0,null,2025-11-01T10:03:41.067Z,false,null,7102,null",
   "errors":[]
}
```

## Important
- 쓸데없이 복잡도 높이는 오버 엔지니어링 하지 마십시오.
- 패키지 레이아웃을 유지하십시오.
