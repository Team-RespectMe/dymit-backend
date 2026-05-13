# BACKLOG
---
## TASK 3: Image Thumbnail Issue.

**STATUS** Done

**BACKGROUND**
아이폰에서 가로모드로 촬영한 이미지가 파일 업로드 API를 통해 전달되면 썸네일이 좌측으로 90도 돌아간 상태로 생성이 됩니다.
가로가 더 긴 이미지에 대해서 이렇게 생성되는 것이 아닙니다. 아이폰 13 미니 기준으로 3024x4032 이미지인데 이렇게 생성되네요.
회전이 되지않고 이미지만 비율을 유지한 상태로 축소만 된 이미지를 썸네일로 생성하도록 수정이 필요합니다.

**REVIEW TARGET**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/ports/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/controllers/files/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/domain/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/application/file/**
- dymit-backend-api/src/main/kotlin/net/noti_me/dymit/dymit_backend_api/adapter/file/**
