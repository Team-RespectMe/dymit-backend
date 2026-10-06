# BACKLOG
---
## HOTFIX - Calendar API 일일 일정 응답 수정
상태: Ready

## OBJECTIVE

현재 Calendar API의 일간 일정 조회 API의 응답은 
TASK와 STUDY_SCHEDULE 두가지가 있습니다.

현재 하나의 API로 이 두 Feature를 모두 공통으로 반환합니다.
하지만 각 요소에 추가 필드가 필요합니다.

- Task 에는 submissionType [제출 타입 태스크 도메인 참고]
- STUDY_SCHEDULE에는 장소 정보가 필요합니다.(대면/비대면 + URL or Location 정보)

따라서 Calendar API의 일간 일정 조회 API의 리스트 응답에 들어가는 데이터 객체에
HATEOAS 관련 클래스(이미 제가 정의해둠) 상속받아 _links 필드에 위 두 값을 각각 채워넣는 방식이 어떨까 합니다.
의견을 제시해주세요.

## REQUIREMENTS
- 요청 URL과 HTTP Method는 유지합니다.
- 응답 예시는 새로 작성해야합니다.

## Important
- 쓸데없이 복잡도 높이는 오버 엔지니어링 하지 마십시오.
- 패키지 레이아웃을 유지하십시오.
