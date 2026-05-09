# BACKLOG
---
## TASK 1: File API 요구사항 변경 

**상태** 완료

**배경** 

현 프로젝트에서 스터디 그룹에서 자료를 공유하는 경우 이미지 파일의 경우 Thumbnail을 생성하여 첨부파일 목록 확인 시에 실제 파일을 다운로드 하지 않고 Thumbnail URL 데이터를 LOAD 하여 트래픽 비용을 줄여야한다. 
또한 지원하는 파일 형식은 JPEG/JPG/PDF 로 제한하며 파일 매직 넘버들을 확인하여 검증해야한다.

1. 현재 File Entity는 Thumbnail 을 위한 URL이 존재하지 않는다. 이를 해결해야한다.
2. File API의 파일 정보 조회 시 이미지 파일이면 Thumbnail이 제공되어야한다.
3. Thumbnail은 S3 Upload 시 URL(/dymit/thumbnails/) 하위로 들어가야한다.
4. Thumbnailator 등 라이브러리 이용할 것

**검토 대상**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/ports/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/controllers/files/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/domain/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/application/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/adapter/file/**

## TASK 2: Study Schedule에 공유 파일(Attachments) 기능 추가

**상태** 완료 

**배경**
현재 스터디 그룹 일정에는 파일 공유 기능이 존재하지 않는다. 일정 별 필요한 학습 자료를 올릴 수 있는 기능이 필요하다. 

1. 공유 자료는 /api/v1/study-schedules/<schedule-id>/attachments 를 PUT으로 일괄 UDPATE 하는 
방식이어야 한다.
2. 일괄 업데이트 시 기존 파일 목록과 비교를 하여 삭제된 경우 해당 파일 엔티티의 상태를 업데이트 해야한다.
3. 일정에 연결된 파일 엔티티는 link 되었음을 업데이트해야한다.
4. 공유 자료 목록 조회는 GET /api/v1/study-schedules/<schedule-id>/attachments 를 통해 이루어져야한다.
5. 공유 자료 목록에는 실제 파일을 다운로드하기 위한 Access URL이 포함되어야 한다.
6. 스터디 일정 상세 조회 API의 응답이 HATEOAS 형식이 지원되는지 확인하고 지원한다면 attachments 키를 추가하고 
url 에 해당 일정의 공유자료 URL을 연결한다.
7. 스터디 일정 관련 기능은 절대 수정하지 말 것
8. schedule_attachements 라는 별도 컬렉션 정의 후 작업

**검토 대상**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/controllers/study-schedule/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/application/study_schedule/**
- 기타 study_schedule 관련 
