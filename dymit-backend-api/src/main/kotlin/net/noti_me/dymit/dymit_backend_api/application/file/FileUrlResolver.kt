package net.noti_me.dymit.dymit_backend_api.application.file

import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import org.springframework.stereotype.Component

/**
 * 파일 상대 경로를 CDN 접근 URL로 변환하는 컴포넌트입니다.
 *
 * @param cdnConfig CDN 설정 정보
 */
@Component
class FileUrlResolver(
    private val cdnConfig: CDNConfig
) {

    /**
     * 상대 경로를 접근 URL로 변환합니다.
     *
     * @param path 파일 상대 경로
     * @return 접근 URL
     */
    fun resolve(path: String): String {
        return cdnConfig.getDomain().trimEnd('/') + path
    }

    /**
     * nullable 상대 경로를 접근 URL로 변환합니다.
     *
     * @param path nullable 파일 상대 경로
     * @return 접근 URL, 없으면 null
     */
    fun resolveOrNull(path: String?): String? {
        return path?.let(::resolve)
    }
}
