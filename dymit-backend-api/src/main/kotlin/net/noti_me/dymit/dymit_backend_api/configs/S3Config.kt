package net.noti_me.dymit.dymit_backend_api.configs

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

/**
 * S3 업로드 설정을 제공하는 설정 클래스입니다.
 *
 * @param bucketName 업로드 대상 S3 버킷 이름
 * @param originDomain S3 origin 접근 도메인
 */
@Configuration
class S3Config(
    @Value("\${cdn.origin.bucketName}") private val bucketName: String,
    @Value("\${cdn.origin.domain}") private val originDomain: String
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        logger.info("S3Config initialized with bucketName='{}', originDomain='{}'", bucketName, originDomain)
    }

    fun getBucketName(): String {
        return bucketName
    }

    fun getOriginDomain(): String {
        return originDomain
    }
}
