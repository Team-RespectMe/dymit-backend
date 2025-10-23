package net.noti_me.dymit.dymit_backend_api.application.file

import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import org.springframework.stereotype.Service

/**
 * url을 CDN용 url로 변환해주는 클래스
 * originalUrl: https://dymit-storage.s3.ap-northeast-2.amazonaws.com/bucket-name/files/uuid-filename.ext
 * cdnUrl: https://cdn.dymit.me/files/uuid-filename.ext
 * 기본적으로 bucket-name부터 포함하여 파일 경로를 구성
 */
@Service
class CDNTranslator(
    private val cndConfig: CDNConfig
) {

    fun toCDNUrl(originalUrl: String): String {
        val cdnDomain = cndConfig.getDomain()
        val filePath = originalUrl.substringAfter(".com/")
        return "$cdnDomain/$filePath"
    }
}
