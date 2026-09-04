# BACKLOG
---
## TASK-100 스터디 모집 공고 정렬 조건 변경
**STATUS** Ready
**BACKGROUND**
- /api/v2/study-recruitments 에서 Dymit 의 Study Recruitments 를 조회하는 분기에서
MongoDB로 날리는 쿼리의 정렬 조건을 현재 bumpAt 기준으로 하는 것을 우선 updatedAt 기준으로 최신 순 정렬하도록 하고
기존 bumpAt 정렬 조건은 주석 처리해두십시오. 추후 bump 기능을 프론트엔드에서 포함하면 정렬 조건을 변경할 예정입니다.

## Important
- 쓸데없이 함수 추가하고 복잡도 높이는 오버 엔지니어링 하지 마십시오.
