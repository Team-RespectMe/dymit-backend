package net.noti_me.dymit.dymit_backend_api.units.adapter.persistence.mongo.report

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.AnnotationSpec
import io.kotest.extensions.spring.SpringExtension
import io.kotest.matchers.shouldBe
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.report.MongoReportRepository
import net.noti_me.dymit.dymit_backend_api.configs.MongoConfig
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import net.noti_me.dymit.dymit_backend_api.domain.report.ResourceType
import org.bson.types.ObjectId
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.data.mongodb.core.MongoTemplate

/**
 * MongoReportRepository 구현체에 대한 테스트 클래스
 * MongoDB를 사용한 신고 정보의 저장, 조회 기능을 테스트합니다.
 */
@DataMongoTest
@Import(MongoConfig::class)
class MongoReportRepositoryTest(
    /**
     * MongoDB 연동을 위한 MongoTemplate
     */
    private val mongoTemplate: MongoTemplate
) : AnnotationSpec() {

    /**
     * 테스트 대상 MongoReportRepository 인스턴스
     */
    private val mongoReportRepository = MongoReportRepository(mongoTemplate)

    /**
     * 테스트용 회원 ID
     */
    private val testMemberId = ObjectId()

    /**
     * 테스트용 리소스 ID
     */
    private val testResourceId = ObjectId().toHexString()

    override fun extensions(): List<Extension> {
        return listOf(
            SpringExtension
        )
    }

    /**
     * 각 테스트 실행 전 데이터베이스 초기화
     */
    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection(Report::class.java)
    }

    /**
     * 각 테스트 실행 후 데이터베이스 정리
     */
    @AfterEach
    fun tearDown() {
        mongoTemplate.dropCollection(Report::class.java)
    }

    /**
     * 테스트용 Report 엔티티를 생성합니다.
     *
     * @param title 신고 제목
     * @param content 신고 내용
     * @param status 처리 상태
     * @return 생성된 Report 엔티티
     */
    private fun createTestReport(
        title: String = "부적절한 제목입니다.",
        content: String = "부적절한 내용입니다.",
        status: ProcessStatus = ProcessStatus.REPORTED
    ): Report {
        val resource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = testResourceId
        )

        return Report(
            memberId = testMemberId,
            resource = resource,
            title = title,
            content = content,
            status = status
        )
    }

    @Test
    fun `신고 정보 저장 테스트`() {
        // Given
        val report = createTestReport()

        // When
        val savedReport = mongoReportRepository.save(report)

        // Then
        savedReport.shouldNotBeNull()
        savedReport.memberId shouldBe testMemberId
        savedReport.title shouldBe "부적절한 제목입니다."
        savedReport.content shouldBe "부적절한 내용입니다."
        savedReport.status shouldBe ProcessStatus.REPORTED
        savedReport.resource.resourceType shouldBe ResourceType.STUDY_GROUP
        savedReport.resource.resourceId shouldBe testResourceId
    }

    @Test
    fun `ID로 신고 정보 조회 테스트`() {
        // Given
        val report = createTestReport()
        val savedReport = mongoTemplate.save(report)

        // When
        val foundReport = mongoReportRepository.findById(savedReport.id)

        // Then
        foundReport.shouldNotBeNull()
        foundReport.id shouldBe savedReport.id
        foundReport.memberId shouldBe testMemberId
        foundReport.title shouldBe "부적절한 제목입니다."
        foundReport.content shouldBe "부적절한 내용입니다."
        foundReport.status shouldBe ProcessStatus.REPORTED
    }

    @Test
    fun `존재하지 않는 ID로 조회시 null 반환 테스트`() {
        // Given
        val nonExistentId = ObjectId()

        // When
        val foundReport = mongoReportRepository.findById(nonExistentId)

        // Then
        foundReport.shouldBeNull()
    }

    @Test
    fun `최근 시간순 리스트 조회 테스트`() {
        // Given - 시간차를 두고 Report 저장
        val report1 = createTestReport("첫 번째 신고", "부적절한 내용1", ProcessStatus.REPORTED)
        val savedReport1 = mongoReportRepository.save(report1)

        Thread.sleep(10) // ObjectId 생성 시간 차이 확보
        val report2 = createTestReport("두 번째 신고", "부적절한 내용2", ProcessStatus.PROCESSED)
        val savedReport2 = mongoReportRepository.save(report2)

        Thread.sleep(10)
        val report3 = createTestReport("세 번째 신고", "부적절한 내용3", ProcessStatus.REJECTED)
        val savedReport3 = mongoReportRepository.save(report3)

        // When
        val reports = mongoReportRepository.findAllOrderByCreatedAtDesc(null, 10)

        // Then
        reports shouldHaveSize 3

        // ObjectId 기준 내림차순 정렬 확인 (최신 것부터)
        reports[0].id shouldBe savedReport3.id
        reports[1].id shouldBe savedReport2.id
        reports[2].id shouldBe savedReport1.id

        // 내용으로도 검증
        reports[0].content shouldBe "부적절한 내용3"
        reports[1].content shouldBe "부적절한 내용2"
        reports[2].content shouldBe "부적절한 내용1"
    }

    @Test
    fun `커서 기반 페이징 테스트`() {
        // Given - 여러 Report 저장
        val reports = mutableListOf<Report>()
        repeat(5) { index ->
            val report = createTestReport("신고 ${index + 1}", "내용 ${index + 1}", ProcessStatus.REPORTED)
            Thread.sleep(10) // ObjectId 생성 시간 차이 확보
            reports.add(mongoReportRepository.save(report))
        }

        // When - 첫 페이지 조회 (사이즈 3)
        val firstPage = mongoReportRepository.findAllOrderByCreatedAtDesc(null, 3)

        // Then
        firstPage shouldHaveSize 3
        // 최신순으로 정렬되어야 함
        firstPage[0].content shouldBe "내용 5"
        firstPage[1].content shouldBe "내용 4"
        firstPage[2].content shouldBe "내용 3"

        // When - 두 번째 페이지 조회 (커서 사용)
        val cursor = firstPage.last().id.toHexString()
        val secondPage = mongoReportRepository.findAllOrderByCreatedAtDesc(cursor, 3)

        // Then
        secondPage shouldHaveSize 2 // 남은 2개
        secondPage[0].content shouldBe "내용 2"
        secondPage[1].content shouldBe "내용 1"
    }

    @Test
    fun `빈 데이터베이스에서 리스트 조회시 빈 목록 반환 테스트`() {
        // When
        val reports = mongoReportRepository.findAllOrderByCreatedAtDesc(null, 10)

        // Then
        reports shouldHaveSize 0
    }

    @Test
    fun `다양한 상태의 신고 정보 저장 및 조회 테스트`() {
        // Given
        val reportedReport = createTestReport("신고 상태", "부적절한 내용", ProcessStatus.REPORTED)
        val processedReport = createTestReport("처리 상태", "부적절한 내용", ProcessStatus.PROCESSED)
        val rejectedReport = createTestReport("거절 상태", "부적절한 내용", ProcessStatus.REJECTED)

        // When
        val savedReported = mongoReportRepository.save(reportedReport)
        val savedProcessed = mongoReportRepository.save(processedReport)
        val savedRejected = mongoReportRepository.save(rejectedReport)

        // Then
        savedReported.shouldNotBeNull()
        savedReported.status shouldBe ProcessStatus.REPORTED

        savedProcessed.shouldNotBeNull()
        savedProcessed.status shouldBe ProcessStatus.PROCESSED

        savedRejected.shouldNotBeNull()
        savedRejected.status shouldBe ProcessStatus.REJECTED

        // 전체 조회로 검증
        val allReports = mongoReportRepository.findAllOrderByCreatedAtDesc(null, 10)
        allReports shouldHaveSize 3
    }
}
