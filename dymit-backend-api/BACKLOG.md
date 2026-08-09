# BACKLOG
---
## TASK-87 Micrometer MDC 연동
**STATUS** Done
**BACKGROUND**

MDCLoggingFilter 를 이용해 직접 traceId 를 생성하던 것을 
micrometer 를 이용한 자동 주입으로 변경하려고 합니다. 

구조를 [PROJECT_STRUCTURE.md](.agent-dev/PROJECT_STRUCTURE.md)를 참고하여 
논리적으로만 모듈로 모노리스를 사용합니다.

**REQUIREMENTS**
- build.gradle.kts 에 필요한 의존성을 추가하십시오.
- application.yaml 에 필요한 설정을 추가하십시오.
- 로거의 출력 확인 테스트는 가능하지만 메인 소스코드는 수정하지 마십시오. 작업 상 부득이하게 수정이 필요하다면 저에게 질문한 뒤 진행하세요.
