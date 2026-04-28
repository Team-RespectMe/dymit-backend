package net.noti_me.dymit.dymit_backend_api.common.pagination

import jakarta.servlet.http.HttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * CursorPagination을 위한 Next URL 빌더 클래스
 *
 * 기능:
 * 1. 현재 엔드포인트를 가져올 수 있습니다 (QueryString 제외)
 * 2. 리스트를 입력으로 받고, next url을 빌드할 수 있습니다
 * 3. 리스트 내 원소의 어떤 필드를 커서 대상으로 잡을지 입력으로 받을 수 있습니다
 * 4. 프록시 환경(Nginx) 뒤에서도 올바른 URL 생성
 */
class CursorNextUrlBuilder {

    companion object {
        /**
         * 현재 요청의 기본 URL을 가져옵니다 (QueryString 제외)
         * 프록시 환경을 고려하여 X-Forwarded-* 헤더를 우선 사용합니다
         */
        fun getCurrentBaseUrl(): String {
            val request = (RequestContextHolder.currentRequestAttributes() as ServletRequestAttributes).request

            // 스키마 결정 로직
            val scheme = getScheme(request)

            val serverName = request.getHeader("X-Forwarded-Host")
                ?: request.getHeader("Host")?.substringBefore(':')
                ?: request.serverName

            // X-Forwarded-Port가 있으면 사용, 없으면 프록시 환경에서는 포트 생략
            val forwardedPort = request.getHeader("X-Forwarded-Port")
            val contextPath = request.contextPath
            val servletPath = request.servletPath

            val url = StringBuilder()
            url.append(scheme).append("://").append(serverName)

            // 포트 처리: X-Forwarded-Port가 있거나, 프록시가 아닌 환경에서만 포트 추가
            if (forwardedPort != null) {
                val port = forwardedPort.toIntOrNull()
                if (port != null &&
                    !((scheme == "http" && port == 80) || (scheme == "https" && port == 443))) {
                    url.append(":").append(port)
                }
            } else if (request.getHeader("X-Forwarded-Host") == null) {
                // 프록시 환경이 아닌 경우에만 직접 포트 확인
                val serverPort = request.serverPort
                if (!((scheme == "http" && serverPort == 80) || (scheme == "https" && serverPort == 443))) {
                    url.append(":").append(serverPort)
                }
            }

            url.append(contextPath).append(servletPath)
            return url.toString()
        }

        /**
         * 요청에서 올바른 스키마를 결정합니다.
         * localhost 환경이면 http, 그 외엔 https 사용
         */
        private fun getScheme(request: HttpServletRequest): String {
            val serverName = request.getHeader("X-Forwarded-Host")
                ?: request.getHeader("Host")?.substringBefore(':')
                ?: request.serverName

            // localhost 또는 127.0.0.1인 경우에만 http 사용, 그 외엔 모두 https
            return if (serverName.contains("localhost") || serverName.contains("127.0.0.1")) {
                "http"
            } else {
                "https"
            }
        }

        /**
         * 람다 함수를 사용하여 커서 값을 추출하고 next URL을 생성합니다
         *
         * @param items 페이지네이션 대상 리스트 (실제로는 size + 1개를 조회해야 함)
         * @param extractors 커서 값을 추출하는 람다 함수들의 맵
         * @param size 페이지 크기
         * @return next URL 문자열 (다음 페이지가 없는 경우 null)
         */
        fun <T> buildNextUrlWithExtractor(
            items: List<T>,
            extractors: Map<String, (T) -> Any>,
            size: Int
        ): String? {
            // size + 1개를 조회했는데 실제로 size + 1개가 있어야만 다음 페이지 존재
            if (items.size <= size) {
                return null // 다음 페이지 없음
            }

            // size번째 아이템(0-based에서 size-1 인덱스)의 커서 값을 사용
            val cursorItem = items[size - 1]
            val baseUrl = getCurrentBaseUrl()
            val queryParams = mutableMapOf<String, String>()

            extractors.forEach { (key, extractor) ->
                val value = extractor(cursorItem).toString()
                queryParams[key] = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
            }
            val queryString = queryParams.entries.joinToString("&") { "${it.key}=${it.value}" }
            return "$baseUrl?$queryString"
        }
    }
}
