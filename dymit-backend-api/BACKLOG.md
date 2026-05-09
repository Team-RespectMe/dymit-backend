# BACKLOG
---
## TASK 3: File API 요구사항 변경 

**상태** 완료

**배경**

File API 에서 파일 업로드 시 지원 파일 목록에 PNG가 추가되어야 합니다.

- 이미지 파일이므로 썸네일 생성 로직은 동일하게 적용되어야 합니다.
- 이미지 처리 로직에서 아마 PNG 매직넘버 체크를 하는 부분만 추가되면 될 것 같으니 확인.

**검토 대상**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/ports/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/controllers/files/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/domain/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/application/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/adapter/file/**
