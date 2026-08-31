# BACKLOG
---
## TASK-96 스터디 상세 조회 시 최신 공지사항 첨부
**STATUS** Done

GET  /api/v1/study-groups/{groupId}
으로 조회되는 엔드포인트의 경우 해당 그룹의 최신 공지사항의 제목을 
포함하여 반환해야하는데 현재 공지사항이 포함되지 않는 문제가 있습니다.

study_group 도메인의 컨트롤러에서 이 부분을 처리하도록 작성해주세요.

StudyGroup Entity에 공지사항 게시판의 아이디가 저장되어있으니 이를 참조하여 구현하세요.

### REQUIREMENTS
- study-group 도메인 패키지 내 application/port/out 에 LoadStudyGroupPostPort 를 정의하십시오.
- study-group 도메인 패키지 내 adapter/out/study_group_posts/LoadStudyGroupPostAdapter 를 정의하십시오. MongoTemplate 을 이용하여 게시판의 최신 글을 가져옵니다.
- 위 엔드포인트에서는 정의한 Port를 이용하여 최신 게시물의 제목을 현재 반환중인 응답 Response 객체의 필드에 채워넣으십시오.
