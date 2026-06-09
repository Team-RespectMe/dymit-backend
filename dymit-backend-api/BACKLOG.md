# BACKLOG
---

## TASK 58-2 이벤트 포함 객체 추가
**STATUS** Done
**BACKGROUND**
TaskCreatedEvent, TaskModifiedEvent, TaskDeletedEvent 각 이벤트에 Task 객체를 포함시키는 것이 좋을듯 합니다. 
그리고 각 이벤트 발행 유즈케이스에서 group 객체가 조회된다면 그것도 포함시키세요.

## TASK 58 과제 생성, 수정, 삭제 시 이벤트 발행
**STATUS** Done
**BACKGROUND**
과제 생성 시 발행 되어야 하는 이벤트 목록에 대해 발행 여부를 확인하고 
발행되지 않았다면 다음 이벤트들을 각 유즈케이스의 구현 및 도메인 엔티티 중 적합한 부분에
추가가 필요합니다.
단, 이벤트는 유즈케이스 1회 실행 당 한번만 발행되어야 합니다.
이름은 아래 제안한 것 외에 이미 사용하는 이벤트가 있다면 그걸로 대체해도 되며 더 적절한
이름으로 사용해도 됩니다.
- 과제 생성 이벤트 (TaskCreatedEvent)
    - 참조 파일: CreateTaskUseCaseImpl
- 과제 수정 이벤트 (TaskModifiedEvent)
    - 참조 파일: UpdateTaskUseCaseImpl
- 과제 삭제 이벤트 (TaskDeletedEvent)
    - 참조 파일: RemoveTaskUseCaseImpl
    - 이벤트에 포함되어야 하는 정보
      - 과제 제출 대상자들의 사용자 ID
