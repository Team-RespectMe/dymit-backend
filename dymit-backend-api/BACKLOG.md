# BACKLOG
---
## TASK-61
**STATUS** Done
**BACKGROUND**
과제 제출물에 댓글이 달린 경우 과제 제출자에게 Push알림을 전송해야 합니다.
TaskSubmissionCommentCreated 이벤트를 정의하고 
이 이벤트 핸들러에서 TaskSubmissionCommentCreatedBroadcastEvent 를 발행합니다.
메시지 내용은 $member.nickname 님이 회원님의 과제 제출에 댓글을 달았습니다.
로 하십시오.
마찬가지로 과제 제출 페이지로 곧바로 이동하기 위해 
UserFeed 에 AssociatedResource로 groupId, taskId, assigneeId 가 포함되어야 합니다.
PushMessage 에는 data 부분에 mapOf 로 동일하게 포함시킵니다.
**REFERENCE**
[1] TaskCreatedBroadcastEvent
[2] TaskCreatedEvent
[3] TaskNotificationPreparationEventHandler
