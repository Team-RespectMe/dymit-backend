# BACKLOG
---
## TASK-63.1 Task Entity 필드 추가 및 요청/응답, DTO들에도 필드 추가
**STATUS** Done
**BACKGROUND**
현재 Task Entity에는 사전 과제/사후 과제를 나타내는 필드인 type만 존재합니다.
비지니스 요구 조건에서 이 Task의 제출 방식이 단순 체크형과 산출물 요구형인지 구분할 수 있는
submissionType 필드 추가 요청이 왔습니다.
SubmissionType 에는 CHECK/OUTPUT 두가지 값을 가질 수 있습니다.
CHECK: 체크형으로, 과제 제출 시 체크박스를 클릭해야 합니다.
OUTPUT: 산출물 요구형으로, 현재 구현 사항에 해당합니다.

1. 과제 생성,수정,삭제,조회에 연관된 모든 플로우에서 새로운 엔티티 타입에 맞게 동작하도록
   코드를 수정하십시오.
2. 과제 Entity의 제출 타입은 수정 시 변경이 불가능해야합니다.
---
## TASK-63.2 Task Submission 관련 비지니스 로직 분기
**STATUS** Done
**BACKGROUND**
과제 제출물의 생성 수정 및 삭제 시 Task Entity의 submissionType에 따른 동작 분기가 필요합니다.
1. TaskEntity가 CHECK 타입이라면
과제 제출 생성 시: TaskAssignee의 제출 상태만 제출됨으로 변경합니다. 산출물 엔티티를 생성하거나 영속화해선 안됩니다.
과제 제출 수정 시: 수정은 허용되지 않습니다.
과제 제출 삭제 시: TaskAssignee의 제출 상태만 미제출 상태로 변경합니다. 산출물 엔티티를 생성하거나 영속화해선 안됩니다.
               Check형 타입에 대한 삭제 요청은 /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions?assigneeId={assigneeId} 형태로 들어오므로 기존 유즈케이스나 서비스 코드를 수정하지 말고 컨트롤러에서 분기할 수 있도록 새로운 유즈케이스를 생성하세요.
2. TaskEntity가 OUTPUT 타입이라면
기존 로직과 동일합니다.
필요하다면 유즈케이스를 분리해도 좋습니다만 판단에 맡기겠습니다.
---
## Task-63.3 Task Submission CRUD 엔드포인트 패턴 통일
**STATUS** Done
**BACKGROUND**
과제 제출물 CRUD 관련 엔드포인트의 패턴이 일관되지 않았습니다. 
아래와 같은 방식으로 통일하십시오.

***현재***
과제 제출물 생성
POST /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions
과제 제출물 수정
PUT /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions/{submissionId}
과제 제출물 삭제
DELETE /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions/{submissionId}
과제 제출물 조회
GET /api/v1/study-groups/{groupId}/tasks/{taskId}/assignees/{assigneeId}/submissions

**요구 패턴**
과제 제출물 생성
POST /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions
과제 제출물 수정
PUT /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions/{submissionId}
과제 제출물 삭제
DELETE /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions/{submissionId}
DELETE /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions?assigneeId={assigneeId}
과제 제출물 조회
GET /api/v1/study-groups/{groupId}/tasks/{taskId}/submissions?assigneeId={assigneeId}
