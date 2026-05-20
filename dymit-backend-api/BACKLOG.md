# BACKLOG
---
## TASK 48: 더이상 참조되지 않는 user file 엔티티 상태의 변경

**STATUS** Done

**BACKGROUND**
스터디 일정에 공유된 파일이 공유 해제 되는 경우 해당 유저 파일의 상태 
역시 어플리케이션 어느 영역에서도 참조되지 않음을 표기해야합니다.
또한 S3에서 삭제된 파일 역시 삭제되었음을 status에 표기할 수 있어야 합니다.
우선 이 작업에서는 스터디 일정 공유 파일(attachments) 서비스에 의해 첨부 파일 목록이
제거되는 경우 이를 체크하여 유저 파일 엔티티의 상태를 더이상 참조되지 않는 다고 status를 업데이트 하는 로직을 추가하세요.
그리고 이를 위한 적절한 상태들도 추가해두세요.
기존 인터페이스와 호환되어야 합니다. 특히 엔티티의 기존 필드가 가질 수 있는 값을 완전히 변경하거나 하지 마십시오.

**LOOK UP**
application/study_schedule/StudyScheduleAttachmentService.kt
application/study_schedule/impl/StudyScheduleAttachmentServiceImpl.kt
application/file/usecases/**
domain/file/UserFile.kt
domain/file/UserFileStatus.kt