# BACKLOG
---
## TASK-85 일일 통계 수치 배치 작업
**STATUS** Done
**BACKGROUND**

사용자의 일일 통계를 내기위한 배치 작업을 구성하려고 합니다.
대상은 
오늘의 회원 가입자, 탈퇴자, 방문자(리프레시 토큰을 통해 액세스 토큰을 발급 받은 경우 updated_at 필드가 업데이트 되는 것으로 알고 있음 확인 필요) 
오늘 생성된 스터디 그룹의 수
오늘 생성된 스터디 일정의 수
오늘 생성된 스터디 과제의 수
오늘 스터디 일정에 참여한 회원의 수
오늘 제출된 과제의 수

정도입니다.

구조를 [PROJECT_STRUCTURE.md](.agent-dev/PROJECT_STRUCTURE.md)를 참고하여 
논리적으로만 모듈로 모노리스를 사용할 것이므로 별도의 의존성 추가 등은 하지 마십시오.

**REQUIREMENTS**
- 당장은 REST Controller는 필요없습니다.
- 각 통계 수치에 필요한 값은 각자의 도메인에서 쿼츠를 통해 수행해서 구합니다.
- 통계 수치는 daily_stats라는 컬렉션에 저장되어야 합니다. 여러 도메인에서 각자의 통계값을 구해서  daily_stats에 업데이트해야 하므로 동시성으로 갱신 유실이 발생하지 않아야합니다. 
- 매일 오전 4시에 해당 시점으로부터 24시간 이전을 시작점으로하여 통계 수치를 구해야합니다.
- daily_stats 에 새로운 도큐먼트 추가가 성공하면 Discord 로 메시지를 전송합니다. 이 로거가 발행해야하는 webhook url은 application.yaml 에 discord.daily_statistics.webhook.url 로 정의되어있습니다.
- 디스코드로 내보내는 기능은 common.logging.discord 내에 몇개가 존재합니다. 지금 생각해보니 메시지 포맷과 url만 다른데 하나의 discord 메시지 전송 기능을 담당하는 클래스와
  메시지 포맷으로 분리하여 사용할 수 있는지 확인하고 그렇게 수정해주세요. 단, 이미 사용중인 부분들에서 이용하는 부분에 문제가 없어야합니다.
- 회원 관련 통계는 현재 member 도메인의 application/batch/DailyMemberStatusJob 에 정의되어 있는 작업이 있습니다. 다른 기능들도 유사하게 작성하세요. 단 여기에는 현재 daily_stats에 저장하는 기능은 없습니다.
- port.in, adapter.in 이런 것들이 in 이 Kotlin 키워드와 충돌한다고 임의로 이름 짓지 마세요 그대로 쓰십시오.
