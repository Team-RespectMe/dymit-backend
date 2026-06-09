# BACKLOG
---
## TASK-60.2 
**STATUS** Done
**BACKGROUND**
과제 제출 생성 시 이벤트 발행되도록 이벤트 정의 및 생성
TaskSubmissionCreatedEvent 등으로 정의 

이 이벤트는 핸들러에서 받아 TaskSubmissionCreatedBroadcastEvent 를 발행
TaskSubmissionCreatedBroadcastEvent 는 BroadcastEvent를 상속받아야 한다.

Feed 및 Push 메시지 대상은 
제출자를 제외한 Assignee 전원이다.

메시지는 ${group.name}의 과제 ${task.name}를 ${member.nickname} 님이 제출했어요. 
Push에는 groupId/taskId/assigneeId 가 포함되어야함.
Feed 메시지에도 마찬가지고 AssociatedResource 에 해당 내용이 포함되어야함.

**Reference**
TaskCreatedEvent
TaskCreatedBroadcastEvent
TaskNotificationPreparationEventHandler
