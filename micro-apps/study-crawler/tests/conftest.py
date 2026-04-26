"""pytest 실행 시 프로젝트 루트 경로를 import 경로에 추가하는 설정.

기능:
- 테스트에서 app 패키지를 안정적으로 import 할 수 있도록 sys.path를 보정합니다.

매개변수:
- 없음(pytest가 자동 로드)

반환형:
- 없음
"""

import sys
from pathlib import Path


PROJECT_ROOT = Path(__file__).resolve().parent.parent
if str(PROJECT_ROOT) not in sys.path:
    sys.path.insert(0, str(PROJECT_ROOT))
