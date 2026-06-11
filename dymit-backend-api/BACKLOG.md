# BACKLOG
---
## TASK-65 Task 생성, 수정 시 비지니스 제약 조건 미구현
**STATUS** Done
**BACKGROUND**

Task 생성 시 결정된 type 에 따라 제출 대상자 구성 방법이 달라져야 합니다.
1. type이 PRE로 결정된 경우 command에 제출된 assigneeIds 는 무시됩니다.
2. type이 PRE로 결정된 경우 assignee는 스케줄 참여자들의 ID를 가져와서 assignee 를 자동 구성해야합니다.
3. type이 POST로 결정된 경우 command의 assigneeIds 를 이용하여 구성해야합니다.

Task 수정 시 type이 POST인 경우에만 제출 대상자 구성을 변경할 수 있습니다.
1. type이 PRE인 과제에 대해 변경 작업을 수행하는 경우 제출 대상자 변경은 무시됩니다.
2. type이 POST이 과제에 대해서만 제출 대상자 구성이 변경 된 경우 변경 작업이 수행되어야 합니다.

**FILE**
CreateTaskUseCaseImpl
UpdateTaskUseCaseImpl
