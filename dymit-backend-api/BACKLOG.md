# BACKLOG
---
## TASK-64.2 Task 생성 시 비지니스 제약 조건
**STATUS** Done
**BACKGROUND** 
Task 생성 시 다음 조건을 만족해야 합니다. 
type 이 PRE(사전과제) 인 과제를 **생성** 하는 경우에는 반드시 일정의 scheduleAt 시점과 24시간 이전인 경우에만 생성 가능해야합니다.

**FILE**
CreateTaskUseCaseImpl

## TASK-64 Task 생성 요청 시 type field 제외
**STATUS** Done
**BACKGROUND**
Task 생성 요청(TaskCommandRequest) 객체에 type 필드가 존재합니다. 
현재 UseCase 구현체가 비지니스 제약 조건을 명확히 구현하였다면 아래 조건을 만족합니다.
1. type 필드는 Task 엔티티 생성 시 결정됩니다. 이후 수정은 불가능합니다.
2. type 필드는 요청 시점과 연관 Schedule 의 scheduleAt 필드 전후 관계를 분석하여
   요청 시점이 아직 일정 전이면 사전 과제, 요청 시점이 일정 후라면 사후 과제로 처리됩니다.

따라서 type 필드는 TaskCommandRequest 에 필요가 없습니다.
이 객체의 필드를 제외하고 연관된 모든 플로우를 검증하십시오.
