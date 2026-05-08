# AGENTS.MD
---
## Project Overview
이 프로젝트는 스터디 그룹 관리 어플리케이션 DYMIT의 백엔드 프로젝트이다.
Kotlin 으로 작성되었으며 JVM 21 을 기준으로 작성되어있다.

## Global Commands

### Build
```bash
gradlew build
```
### Run Test
```bash
gradlew bootTestRun  
```
### Build Docker Image
```bash
gradlew buildDockerImage
```
- Push Docker Image
```bash
gradlew pushDockerImage
```

## Roles

- 각 에이전트들은 프로젝트 하부에 정의된 자신의 역할에 맞는 roles/<RULE>.md 파일을 항상 포함 하여야 합니다.
- 각 에이전트들은 자신의 작업 결과에 대한 로그를 .agent-logs/<RULE>_<BRANCHNAME>_YYYY_MM_DD_HH_MM_SS.md 형태로 남겨 다른 에이전트가 자신의 작업을 이해할 수 있도록 충분히 설명해야 합니다.
- 각 에이전트는 자신의 역할.md 에 작성된 Allowed Actions 에 해당하는 작업만을 수행할 수 있습니다.

### PM
 BACKLOGS.md 파일을 분석하여 필요한 TASKS.md에 작업 목록을 작성하고 이를 기반으로 coder, tester, reviewer 에게 작업을 할당하고 서브에이전트 실행을 하는 역할을 수행합니다. 서브에이전트들의 산출물을 보고 판단하여 작업 지속 여부를 판단합니다.
### Coder 
PM이 작성한 TASK를 직접 구현하는 역할을 수행합니다.
### Tester
PM이 작성한 TASK를 기준으로 Coder가 작성한 로직이 도메인 규칙을 모두 올바르게 구현하였는지 직접 테스트 코드를 작성합니다. 
### Reviewer
PM이 작성한 TASK들을 구현한 Coder Output / Tester Output 을 검증하고 main branch 병합에 문제가 없는지 판단합니다.

## Global Git Rules
- 에이전트들은 브랜치의 생성, 스위치, 삭제, 병합, 푸시를 할 수 없습니다. 
- 에이전트들은 변경사항을 커밋할 수 없습니다.
- 에이전트들은 현재 브랜치에서만 작업할 수 있습니다. 
- 에이전트들은 'git diff', 'git status' 를 inspection 을 위해 사용할 수 있습니다.  
- 모든 sub-agent 들(Coder, Tester, Reviewer)는 현재 브랜치 기준으로만 작업합니다. 

## Global Code Style
- Kotlin Coding Convention 준수
- 함수의 매개변수가 3개 이상이거나, 함수명이 길어져서 가독성이 떨어지는 경우 아래와 같은 형식으로 작성합니다.
```kotlin 
   fun functionName(
         param1: Type1,
         param2: Type2,
         param3: Type3
    ): ReturnType {
         // function body
    }
```
- if 문 반복문의 컨디션 부분은 괄호 시작과 끝에 공백을 추가합니다.
```kotlin
...
if ( condition ) {

}
```
- class 선언 이후 한줄 개행을 합니다.
- docs 생성을 위한 주석은 KDoc 스타일로 작성합니다.
- docs 생성을 위해 클래스 정의 상단에 필드와 생성자에 대한 정보를 명시합니다.
- docs 생성을 위해 메서드 상단에 동작과 매개변수와 반환형에 대한 정보를 명시합니다.
- docs 생성을 위해 인터페이스 역시 상단에 어떤 기능을 하는 인터페이스인지 작성하고, 인터페이스 메서드에도 동작과 매개변수와 반환형에 대한 정보를 명시합니다.
- 한 파일의 최대 크기는 500줄로 제한합니다. 그 이상은 파일을 분리하세요.

## GLOBAL RULES
- 사람이 읽는걸 대상으로 하는 파일은 한글로 작성한다.
- 에이전트 간 통신은 영어로 작성한다.