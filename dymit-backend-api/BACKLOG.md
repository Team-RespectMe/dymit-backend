# BACKLOG
---
## TASK-60 과제 이벤트 핸들러 구현
**STATUS** Done
**BACKGROUND**
TaskCreatedEvent, TaskModifiedEvent, TaskDeletedEvent 에 대한 
구현이 필요합니다.
위 세 이벤트는 각각 BroadcastEvent 를 상속한 별도의 이벤트를 발행하여야 합니다. 
TaskCreatedBroadcastEvent 등으로 네이밍한 별도 이벤트 설계가 필요합니다.

Task**BroadcastEvent 는 사용자별 피드 생성 및 Push 메시지 발행을 위한 이벤트입니다.
각 이벤트는 수신 대상의 ID별로 하나씩 생성되어 리스트 반환됩니다. 중복 방지에 유의하세요. 
각 BroadCastEvent 에는 eventName을 정의하십시오 모두 대문자고 단어는 _ 로 구분합니다.
TASK_CREATED
TASK_MODIFIED
TASK_DELETED 입니다.

- TaskCreatedBroadcastEvent 의 각 피드/메시지는 아래와 같아야 합니다.
  - IconType = "NOTICE",
  - messages = ${group.name}에 새로운 과제가 추가되었어요.
  - associates = AssociatedResource로 앱에서 과제 상세화면으로 리디렉션하기 위해 필요한 리소스들의 ID 정보를 포함해야합니다
    - STUDY_GROUP identifier
    - TASK identifier
- TaskModifiedBroadcastEvent 의 각 피드/메시지는 아래와 같아야 합니다.
  - IconType = "CHECK",
  - messages = ${group.name}의 과제 ${task.title}에 수정된 내용이 있어요.
  - associates = AssociatedResource로 앱에서 과제 상세화면으로 리디렉션하기 위해 필요한 리소스 정보를 포함해야 합니다.
- TaskModifiedBroadcastEvent 의 각 피드/메시지는 아래와 같아야 합니다.
  - iconType = NOTICE
  - messages = ${group.name}의 과제 ${task.title}가 취소되었어요.
  - associates = 스터디 그룹 상세 화면으로 리디렉션 하기 위한 STUDY_GROUP identifier만 필요
연관 정보는 피드의 경우 associates 에 정의하며, 푸시 메시지의 경우 data 필드에 추가합니다.
  - data 필드는 mapOf("groupId" to ..., "taskId" to ...) 형태로 정의하십시오.

- 각 이벤트의 수신 대상
TaskCreatedBroadcastEvent
- Assignee 전체
TaskModifiedBroadcastEvent
- Assignee 전체
TaskDeletedBroadcastEvent
- Assignee 전체, 단 이미 과제가 삭제 된 경우 TaskAssignee들도 모두 삭제되어 있기 때문에 
TaskDeletedEvent 에 포함된 assignee 정보를 기준으로 발행합니다.
