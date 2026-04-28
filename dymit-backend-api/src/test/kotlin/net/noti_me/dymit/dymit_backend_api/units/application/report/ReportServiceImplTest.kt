package net.noti_me.dymit.dymit_backend_api.units.application.report

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.application.report.dto.ReportCommand
import net.noti_me.dymit.dymit_backend_api.application.report.impl.ReportServiceImpl
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.member.MemberRole
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import net.noti_me.dymit.dymit_backend_api.domain.report.ResourceType
import net.noti_me.dymit.dymit_backend_api.ports.persistence.report.ReportRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query

/**
 * ReportServiceImpl에 대한 테스트 클래스
 * 신고 생성, 상태 변경, 목록 조회 등의 비즈니스 로직을 테스트합니다.
 */
class ReportServiceImplTest : BehaviorSpec({

    val reportRepository = mockk<ReportRepository>()
    val mongoTemplate = mockk<MongoTemplate>()
    val reportService = ReportServiceImpl(reportRepository, mongoTemplate)

    val testMemberId = ObjectId()
    val testAdminMemberId = ObjectId()
    val testResourceId = ObjectId().toHexString()

    /**
     * 테스트용 일반 회원 정보를 생성합니다.
     */
    fun createMemberInfo(): MemberInfo {
        return MemberInfo(
            memberId = testMemberId.toHexString(),
            nickname = "testUser",
            roles = listOf(MemberRole.ROLE_MEMBER)
        )
    }

    /**
     * 테스트용 어드민 회원 정보를 생성합니다.
     */
    fun createAdminMemberInfo(): MemberInfo {
        return MemberInfo(
            memberId = testAdminMemberId.toHexString(),
            nickname = "adminUser",
            roles = listOf(MemberRole.ROLE_ADMIN)
        )
    }

    /**
     * 테스트용 ReportCommand를 생성합니다.
     */
    fun createReportCommand(): ReportCommand {
        val resource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = testResourceId
        )
        return ReportCommand(
            title = "부적절한 내용 신고",
            content = "스팸성 게시물입니다.",
            resource = resource
        )
    }

    /**
     * 테스트용 Report 엔티티를 생성합니다.
     */
    fun createReport(
        id: ObjectId = ObjectId(),
        memberId: ObjectId = testMemberId,
        status: ProcessStatus = ProcessStatus.REPORTED
    ): Report {
        val resource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = testResourceId
        )
        return Report(
            id = id,
            memberId = memberId,
            resource = resource,
            title = "부적절한 내용 신고",
            content = "스팸성 게시물입니다.",
            status = status
        )
    }

    given("신고 생성 요청이 주어졌을 때") {
        val memberInfo = createMemberInfo()
        val command = createReportCommand()

        `when`("유효한 회원이 신고를 생성할 때") {
            val savedReport = createReport()
            every { reportRepository.save(any()) } returns savedReport

            val result = reportService.createReport(memberInfo, command)

            then("신고가 성공적으로 생성된다") {
                result.memberId shouldBe testMemberId.toHexString()
                result.title shouldBe "부적절한 내용 신고"
                result.content shouldBe "스팸성 게시물입니다."
                result.status shouldBe ProcessStatus.REPORTED
                verify { reportRepository.save(any()) }
            }
        }
    }

    given("신고 상태 변경 요청이 주어졌을 때") {
        val adminMemberInfo = createAdminMemberInfo()
        val reportId = ObjectId()
        val existingReport = createReport(id = reportId)

        `when`("어드민이 신고 상태를 변경할 때") {
            every { reportRepository.findById(reportId) } returns existingReport
            every { reportRepository.save(any()) } returns existingReport

            val result = reportService.updateReportStatus(adminMemberInfo, reportId, ProcessStatus.PROCESSED)

            then("신고 상태가 성공적으로 변경된다") {
                result.status shouldBe ProcessStatus.PROCESSED
                verify { reportRepository.findById(reportId) }
                verify { reportRepository.save(any()) }
            }
        }

        `when`("일반 회원이 신고 상태를 변경하려고 할 때") {
            val memberInfo = createMemberInfo()

            then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    reportService.updateReportStatus(memberInfo, reportId, ProcessStatus.PROCESSED)
                }.message shouldBe "어드민 권한이 필요합니다."
            }
        }

        `when`("존재하지 않는 신고의 상태를 변경하려고 할 때") {
            val nonExistentId = ObjectId()
            every { reportRepository.findById(nonExistentId) } returns null

            then("NotFoundException이 발생한다") {
                shouldThrow<NotFoundException> {
                    reportService.updateReportStatus(adminMemberInfo, nonExistentId, ProcessStatus.PROCESSED)
                }.message shouldBe "존재하지 않는 신고입니다."
            }
        }
    }

    given("신고 목록 조회 요청이 주어졌을 때") {
        val adminMemberInfo = createAdminMemberInfo()

        `when`("어드민이 첫 페이지를 조회할 때") {
            val reports = listOf(
                createReport(ObjectId(), testMemberId, ProcessStatus.REPORTED),
                createReport(ObjectId(), testMemberId, ProcessStatus.PROCESSED)
            )

            every { reportRepository.findAllOrderByCreatedAtDesc(null, 10) } returns reports

            val result = reportService.getReportList(adminMemberInfo, null, 10)

            then("신고 목록이 성공적으로 조회된다") {
                result.size shouldBe 2
                result[0].status shouldBe ProcessStatus.REPORTED
                result[1].status shouldBe ProcessStatus.PROCESSED
                verify { reportRepository.findAllOrderByCreatedAtDesc(null, 10) }
            }
        }

        `when`("어드민이 커서를 사용하여 페이지를 조회할 때") {
            val cursor = ObjectId().toHexString()
            val reports = listOf(createReport())

            every { reportRepository.findAllOrderByCreatedAtDesc(cursor, 5) } returns reports

            val result = reportService.getReportList(adminMemberInfo, cursor, 5)

            then("커서 기반 페이징이 적용된 목록이 조회된다") {
                result.size shouldBe 1
                verify { reportRepository.findAllOrderByCreatedAtDesc(cursor, 5) }
            }
        }

        `when`("일반 회원이 신고 목록을 조회하려고 할 때") {
            val memberInfo = createMemberInfo()

            then("ForbiddenException이 발생한다") {
                shouldThrow<ForbiddenException> {
                    reportService.getReportList(memberInfo, null, 10)
                }.message shouldBe "어드민 권한이 필요합니다."
            }
        }
    }
})
