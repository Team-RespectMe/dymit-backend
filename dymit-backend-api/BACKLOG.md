# BACKLOG
---
## TASK-68 Task Submission 버그 수정 요청
**STATUS** Done
**BACKGROUND**

과제 제출 시 마감 시간을 오늘 날짜로 지정한 과제에 대해 과제의 제출 혹은 삭제가 
안된다는 버그 리포팅이 들어왔습니다.
의심되는 사항으로는 현재 사용자가 보내는 요청은 UTC+9 기준으로 LocalDateTime 타입으로 요청객체를
받고 있고 사후 과제(사용자가 마감시간을 지정할 수 있음)의 경우 이 값을 UTC+0 로 변환하여 저장하는
것으로 알고있는데 이로인해 문제가 발생하는것 아닌지 확인 부탁드립니다.
서버는 UTC+0 기준으로 동작하고 있으며 MongoDB도 UTC+0 기준입니다.(현재 과제 엔티티의 저장 시 시간 관련 필드들도 UTC0 기준인지 그런지 확인해야할 듯 합니다.)
클라이언트 측이 UTC+9, UTC+0 상관없이 서버가 동작하는 건지 파악도 필요합니다.

**FILE**
CreateTaskUseCaseImpl
CreateSubmissionUseCaseImpl
DeleteSubmissionUseCaseImpl
