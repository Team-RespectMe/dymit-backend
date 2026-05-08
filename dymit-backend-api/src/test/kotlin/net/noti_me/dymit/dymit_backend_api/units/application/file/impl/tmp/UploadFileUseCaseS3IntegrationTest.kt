package net.noti_me.dymit.dymit_backend_api.units.application.file.impl.tmp

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import net.noti_me.dymit.dymit_backend_api.application.file.impl.S3FileService
import net.noti_me.dymit.dymit_backend_api.configs.CDNConfig
import net.noti_me.dymit.dymit_backend_api.configs.S3Config
import org.junit.jupiter.api.Assumptions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.mock.web.MockMultipartFile
import java.util.UUID

/**
 * 실제 S3 업로드를 검증하는 통합 테스트입니다.
 *
 * 환경변수로 AWS 자격증명과 S3 버킷 정보가 준비된 경우에만 실행됩니다.
 */
@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@Import(UploadFileUseCaseS3IntegrationTest.TestAwsConfig::class)
class UploadFileUseCaseS3IntegrationTest(
    private val s3FileService: S3FileService,
    private val amazonS3: AmazonS3,
    private val s3Config: S3Config,
    private val cdnConfig: CDNConfig
) : AnnotationSpec() {

    override fun extensions(): List<Extension> {
        return listOf(SpringExtension)
    }

    @BeforeEach
    fun setUp() {
        Assumptions.assumeTrue(readEnv("AWS_ACCESS_KEY_ID", "DHKIM92_AWS_ACCESS_KEY_ID").isNullOrBlank().not())
        Assumptions.assumeTrue(readEnv("AWS_SECRET_ACCESS_KEY", "DHKIM92_AWS_ACCESS_SECRET_KEY").isNullOrBlank().not())
        Assumptions.assumeTrue(readEnv("DYMIT_S3_BUCKET_NAME").isNullOrBlank().not())
        Assumptions.assumeTrue(readEnv("DYMIT_S3_DOMAIN").isNullOrBlank().not())
    }

    @Test
    fun `실제 S3에 파일을 업로드한다`() {
        val multipartFile = MockMultipartFile(
            "file",
            "integration-test.txt",
            "text/plain",
            "dymit-s3-integration".toByteArray()
        )
        val objectPath = "/dymit/test/${UUID.randomUUID()}/integration-test.txt"

        val result = s3FileService.upload(multipartFile, objectPath)

        try {
            result.path shouldBe objectPath
            result.path shouldStartWith "/dymit/"
            result.accessUrl shouldBe s3Config.getOriginDomain().trimEnd('/') + objectPath

            amazonS3.doesObjectExist(
                s3Config.getBucketName(),
                result.path.removePrefix("/")
            ) shouldBe true
        } finally {
            amazonS3.deleteObject(
                s3Config.getBucketName(),
                result.path.removePrefix("/")
            )
        }
    }

    private fun readEnv(primary: String, fallback: String? = null): String? {
        val primaryValue = System.getenv(primary)
        if (primaryValue.isNullOrBlank().not()) {
            return primaryValue
        }

        return if (fallback == null) {
            null
        } else {
            System.getenv(fallback)
        }
    }

    @TestConfiguration
    class TestAwsConfig {

        @Bean
        @Primary
        fun amazonS3(): AmazonS3 {
            val accessKeyId = readEnv("AWS_ACCESS_KEY_ID", "DHKIM92_AWS_ACCESS_KEY_ID")
                ?: throw IllegalStateException("AWS access key is not configured for S3 integration test.")
            val secretAccessKey = readEnv("AWS_SECRET_ACCESS_KEY", "DHKIM92_AWS_ACCESS_SECRET_KEY")
                ?: throw IllegalStateException("AWS secret key is not configured for S3 integration test.")
            val credentials = BasicAWSCredentials(accessKeyId, secretAccessKey)

            return AmazonS3ClientBuilder.standard()
                .withCredentials(AWSStaticCredentialsProvider(credentials))
                .withRegion(Regions.AP_NORTHEAST_2)
                .build()
        }

        private fun readEnv(primary: String, fallback: String): String? {
            val primaryValue = System.getenv(primary)
            if (primaryValue.isNullOrBlank().not()) {
                return primaryValue
            }

            return System.getenv(fallback)
        }
    }
}
