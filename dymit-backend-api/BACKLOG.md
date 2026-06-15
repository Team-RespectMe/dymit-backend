# BACKLOG
---
## TASK-67 Task 도메인 댓글 정책 변경
**STATUS** Done
**BACKGROUND**

제출된 과제에 피드백을 위해 댓글을 다는 경우 이전 구현에서는 과제 참여자만 댓글을 달 수 있었습니다.
그룹 내 모든 사용자가 과제 댓글을 달 수 있어야 합니다.
현재 댓글쪽은 제가 임의로 수정하여 댓글 CUD 에 대해 과제 제출자 목록에 포함되어있어야만 동작하던 부분을
제거하였습니다
나머지도 확인하고 테스트를 해본 뒤 테스트코드 수정을 해주세요.

**사용자가 수정한 파일**
- CreateSubmissionCommentUseCaseImpl
- DeleteSubmissionCommentUseCaseImpl
- UpdateSubmissionCommentUseCaseImpl
