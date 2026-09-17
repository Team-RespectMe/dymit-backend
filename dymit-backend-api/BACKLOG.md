# BACKLOG
---
## TASK-102 만료된 스터디 모집 공고 삭제
**STATUS** Done
**BACKGROUND**

Dymit Study Recruitment 즉, 이 서비스가 소스인 스터디 그룹 모집(Inflearn 등은 제외)
의 경우 updatedAt 필드가 존재합니다. 또한 상태가 모집 중이 아닌 상태의 모든 모집 공고에 대하여
updatedAt으로부터 24시간이 지난 시점에서 물리적 삭제(deletedAt 필드 업데이트로 삭제하는 것이 아닌 실제 레코드 삭제)
하는 로직이 필요합니다.


## REQUIREMENTS
- Qualtz 를 이용하여 구현하십시오.
- 이 기능은 매 정각마다 한번씩 돌아가야합니다.

## Important
- 쓸데없이 복잡도 높이는 오버 엔지니어링 하지 마십시오.
- 패키지 레이아웃을 유지하십시오.
