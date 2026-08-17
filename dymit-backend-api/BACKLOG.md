# BACKLOG
---
## TASK-90 Dymit 스터디그룹 도메인 엔티티 추가
**STATUS** Done
**BACKGROUND**

현재 스터디 그룹 모집 API는 외부 스터디 그룹에 대한 모집만 조회가 가능합니다.

이제 Dymit 에서 존재하는 스터디 그룹들이 자신의 그룹에 대한 채용공고를 올릴 수 있도록 기능을 추가하려합니다.

### Base Rules
- 구조를 [PROJECT_STRUCTURE.md](.agent-dev/PROJECT_STRUCTURE.md)를 참고하여 
논리적으로만 모듈로 모노리스를 사용합니다.
- /api/v1/study-recruitments 의 동작은 그대로 유지되어야 합니다.


### REQUIREMENTS
- StudyRecuritment 도메인 엔티티는 외부 스터디 채용에 대한 엔티티로 둘 것입니다. 
- DymitStudyRecuritment 라는 새로운 도메인 엔티티를 추가하세요.
- "study_recuritments" 라는 컬렉션에 저장합니다. 즉 StudyRecruitment 와 동일한 컬렉션에 저장할 예정인데 문제가 생기지 않을지 파악하세요.
- 새로운 도메인 엔티티의 필드는 아래와 같습니다.
```json
{
    "_id": ObjectId(...),
    "writer" {
        "id": ObjectId(...),
        "nickname": String,
    },
    "group": {
        "id": ObjectId(...),
        "name": "...",
    },
    "title": "공고 내용", / 50자 제한
    "description": "스터디 소개", / 200자 제한
    "purpose": "스터디 목적", // 50자 제한
    "recruitment_status": "RECRUITING", // ENUM 정의 RECRUITING/DONE, 
    "recruitment_start": Instant?, // 미지정 가능함
    "recruitment_end": Instant?, // 미지정 가능함
    "target_member": "모집 대상", // 100자 제한
    "study_format": "운영 방식", // 100자 제한
    "contact": String, // 연락처 및 연락 URL // 255자 제한
    "createdAt": LocalDateTime? 
    "updatedAt": LocalDateTime?
    "isDeleted": Boolean
}
```
- title, description, purpose, recruitment_start, recruitment_end, target_member, study_format, contact는 수정 가능합니다. 각각에 대해 별도로 변경 함수를 작성하세요. 
- group은 생성 시 확정되는 정보로 수정 불가능합니다.
- writer도 생성 시 확정되는 정보로 수정 불가능합니다.
