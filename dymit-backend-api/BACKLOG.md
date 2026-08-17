# BACKLOG
---
## TASK-90 Dymit 스터디그룹 CRUD 서비스
**STATUS** Done
**BACKGROUND**

현재 스터디 그룹 모집 API는 외부 스터디 그룹에 대한 모집만 조회가 가능합니다.
Dymit StudyGroup 의 채용 공고에 대한 CRUD Feature를 추가하려고 합니다.
이 작업의 결과물은 /api/v2/study-recruitments 패턴의 엔드포인트만을 사용합니다.

### Base Rules
- 구조를 [PROJECT_STRUCTURE.md](.agent-dev/PROJECT_STRUCTURE.md)를 참고하여 
논리적으로만 모듈로 모노리스를 사용합니다.
- /api/v1/study-recruitments 의 동작은 그대로 유지되어야 합니다.


### REQUIREMENTS
- 모든 대상은 DymitStudyRecruitment 를 만들 수 있는 도큐먼트만 대상으로 합니다. 
- DymitStudyRecruitment Entity의 group 필드는 그냥 없애고 단일 groupId로  
대체하는게 좋겠습니다. 대신 tags 필드를 추가하십시오(List<String>), type 필드도
추가하세요.
- type필드의 경우 StudyRecruitmentType 이란걸 정의하고, DYMIT,INFLEARN을
정의하세요. 
- 한 파일에 한 인터페이스 한 클래스 이런식으로 작성하세요 한 파일안에
때려박지말고 
- Dymit Study Recruitment 의 Create 엔드포인트를 생성하십시오.
    - Controller의 입력으로 CreateStudyRecruitmentRequest 를 정의하세요. 
```json 
//CreateStudyRecruitmentRequest
{
    "groupId": "스터디 모집 대상 study group id",
    "description": "스터디 모집 설명",
    "purpose": "스터디 목적",
    "targetMember": "모집 대상 정보",
    "studyFormat" : "스터디 진행 방식에 대한 설명",
    "contact": "url or phone or email",
    "recruitment_start": Instant?,
    "recruitment_end": Instant?,
    "tags": List<String> = emptyList(),
}

```
- Dymit Study Recruitment 의 Put 엔드포인트를 생성하십시오. 
    - UpdateStudyRecruitmentRequest 를 정의하고 사용합니다.
- groupId를 통해 실제 스터디그룹을 조회하고(DymitStudyRecuritmentLoadStudyGroupPort 정의하여 사용)
그 이름을 가져와서 title로 사용합니다.

```json
// UpdateStudyRecruitmentRequest
{
    "description": "스터디 모집 설명",
    "purpose": "스터디 목적",
    "targetMember": "모집 대상 정보",
    "studyFormat" : "스터디 진행 방식에 대한 설명",
    "contact": "url or phone or email",
    "recruitment_start": Instant?,
    "recruitment_end": Instant?,
    "status": "RECRUITING OR DONE",
    "tags": List<String> = emptyList<>()
}
```
- Dymit Study Recruitment 의 Delete 엔드포인트를 생성하십시오.
- Dymit Study Recruitment 의 목록조회 GET Endpoint 를 생성하십시오.
    - RequestParam으로 cursor 를 가지고 있어야하며(required=false) null이 허용됩니다.
    - cursor가 null이면 그냥 최신순 조회합니다.
    - cursor가 존재하면 cursor 이전에 생성된 데이터로 최신순 조회합니다.
    - ListResponse형태로 반환하며, ListResponse.of 로 생성하여 반환합니다. 사용예는 다른 코드들을 참조하세요.
- Dymit Study Recruitment 의 단건 목록 조회 Get Endpoint 를 생성하십시오.
- Dymit Study Recruitment의 CUD는 그룹 소유자 한정으로 가능합니다. 

### REFERENCE
- ./src/main/kotlin/net/noti_me/dymit/dymit_backend_api/study_recruitment/domain/DymitStudyRecuritment.kt
