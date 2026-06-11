# BACKLOG
---
## TASK-66 Task 도메인 스터디 일정 참여 시 동기화 트랜잭션 미구현
**STATUS** Done
**BACKGROUND**

현재 사전 과제가 생성되어 있는 미래의 일정에 참여하는 경우 
브로드캐스트 메시지만 발행하고 과제 제출 대상자에 해당 일정의 신규 참여자를 포함시키는 로직이
제외되어있습니다.
이를 확인하고 구현하십시오.

**FILE**
TaskScheduleSyncEventHandler
ScheduleParticipateEvent
