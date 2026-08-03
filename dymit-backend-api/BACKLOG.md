# BACKLOG
---
## TASK-86 Prometheus 연동
**STATUS** Done
**BACKGROUND**
서버 엔드포인트 별 수행시간과 요청 수 JVM 머신의 상태 등을 추적하기 위한 
Prometheus 관련 설정이 필요합니다. 

구조를 [PROJECT_STRUCTURE.md](.agent-dev/PROJECT_STRUCTURE.md)를 참고하여 
논리적으로만 모듈로 모노리스를 사용합니다.

**REQUIREMENTS**
- 실제 사용자에 의하여 호출되는 엔드포인트들은 수행 시간과 성공 / 실패 비율들을 프로메테우스를 통해 수집할 수 있는지 파악하고 생성합니다.
- 프로메테우스 측에서 각 API 서버 인스턴스의 Liveness 를 파악하는데는 어떤 방식을 사용하는게 좋을지 판단하십시오. Grafana에서 직접 /health-check 를 호출하는게 맞는지 
  아니면 쿠버네티스를 통하는게 좋을지 판단합니다.
- build.gradle.kts 에 필요한 의존성을 추가하십시오.
- port.in, adapter.in 이런 것들이 in 이 Kotlin 키워드와 충돌한다고 임의로 이름 짓지 마세요 그대로 쓰십시오.
