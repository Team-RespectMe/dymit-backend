"""페이지 주소로 GET 요청을 보내 원본 HTML 문자열을 반환하는 유틸리티.

기능:
- 주어진 주소(addr)에 HTTP GET 요청을 보냅니다.
- 응답이 성공(2xx)인 경우 페이지 소스를 문자열로 반환합니다.

매개변수:
- addr (str): 요청할 대상 URL 주소

반환형:
- str: 서버가 반환한 원본 HTML 문자열
"""

import requests


def fetch_page_source(addr: str) -> str:
    response = requests.get(addr, timeout=10)
    response.raise_for_status()
    return response.text
