# BACKLOG
---
## TASK-97 DymitStudyRecruitment 엔티티 기능 추가
**STATUS** Done

**BACKGROUND**

스터디 그룹 모집 공고 요구 사항에 다음과 같은 요구사항이 추가되었습니다.
## REQUIREMENTS 
1. 모집 공고는 생성 시 공고를 만들려는 StudyGroup Id 를 기준으로 이미 올라간 공고가 존재하면 409 Conflict 에러를  발생시켜야 합니다.


2. 스터디 모집공고 엔티티에 필드가 추가되어야 합니다.
bumpAt: Instant,
bumpCount: Integer

두 값을 추가하십시오.

도메인 엔티티의 메서드로 bump 를 추가하고 다음 조건을 따르십시오. 

- bump는 최대 5회 가능합니다.
- bump 호출 시 bumpAt 이 업데이트 되어야 하며, bumpCount 가 1씩 증가합니다.
- bumpCount >= 5 이면 TooManyRequestException(code="EXCEED_BUMP_COUNT", message="끌어올리기 최대 횟수를 초과하였습니다.") 라고 에러를 반환하십시오. 
TooManyRequestException 은 common 패키지 내에 에러 정의 패키지가 있으니 거기에 BusinessException을 상속받아 구현하고 그걸 사용하세요
- bumpCount 는 엔티티 생성 시 0으로 초기화, bumpAt 은  bumpAt = Instant.now() 으로 초기화합니다.
- 끌어올리기 요청은 /api/v2/study-recruitments/{study-recruitments-id}/bumps 로 POST요청을 받아 처리합니다.
- usecase 패키지에 BumpStudyRecuritmentUseCase 를 추가하십시오. 
- 위 유즈케이스의 구현체는 도메인 객체를 조회하여 bump 함수를 호출한 뒤 저장한 뒤, 스터디채용 객체를 반환하고 컨트롤러에서 단건 조회 응답과 같은 응답으로 반환합니다.
- 스터디모집 목록 조회를 할 때 정렬 조건을 bumpAt기준으로 변경하십시오. 최신순으로 정렬합니다.
