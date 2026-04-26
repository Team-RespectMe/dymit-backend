package net.noti_me.dymit.dymit_backend_api.units.domain.report

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import net.noti_me.dymit.dymit_backend_api.domain.report.ProcessStatus
import net.noti_me.dymit.dymit_backend_api.domain.report.Report
import net.noti_me.dymit.dymit_backend_api.domain.report.ReportedResource
import net.noti_me.dymit.dymit_backend_api.domain.report.ResourceType
import org.bson.types.ObjectId

class ReportTest : BehaviorSpec({

    val testMemberId = ObjectId()
    val testResourceId = ObjectId().toHexString()

    given("Report 인스턴스가 주어졌을 때") {
        val memberId = ObjectId()
        val resource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = ObjectId().toHexString()
        )
        val report = Report(
            memberId = memberId,
            resource = resource,
            title = "부적절한 제목입니다.",
            content = "부적절한 내용입니다.",
            status = ProcessStatus.REPORTED
        )

        `when`("Report가 생성될 때") {
            then("기본 ID는 null 상태다") {
                report.id shouldBe null
            }

            then("memberId가 올바르게 설정된다") {
                report.memberId shouldBe memberId
            }

            then("resource가 올바르게 설정된다") {
                report.resource shouldBe resource
                report.resource.resourceType shouldBe ResourceType.STUDY_GROUP
                report.resource.resourceId shouldBe resource.resourceId
            }

            then("title이 올바르게 설정된다") {
                report.title shouldBe "부적절한 제목입니다."
            }

            then("content가 올바르게 설정된다") {
                report.content shouldBe "부적절한 내용입니다."
            }

            then("초기 status가 REPORTED로 설정된다") {
                report.status shouldBe ProcessStatus.REPORTED
            }

            then("BaseAggregateRoot의 속성들이 올바르게 상속된다") {
                report.isDeleted shouldBe false
            }
        }

        `when`("updateStatus 메서드를 호출할 때") {
            then("status가 PROCESSED로 변경된다") {
                report.updateStatus(ProcessStatus.PROCESSED)
                report.status shouldBe ProcessStatus.PROCESSED
            }

            then("status가 REJECTED로 변경된다") {
                report.updateStatus(ProcessStatus.REJECTED)
                report.status shouldBe ProcessStatus.REJECTED
            }

            then("다시 REPORTED로 변경할 수 있다") {
                report.updateStatus(ProcessStatus.REPORTED)
                report.status shouldBe ProcessStatus.REPORTED
            }
        }

        `when`("markAsDeleted 메서드를 호출할 때") {
            then("isDeleted가 true로 변경된다") {
                report.markAsDeleted()
                report.isDeleted shouldBe true
            }
        }
    }

    given("다른 ResourceType으로 Report를 생성할 때") {
        val memberId = ObjectId()
        val resource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = ObjectId().toHexString()
        )

        `when`("STUDY_GROUP 타입의 리소스로 Report를 생성할 때") {
            val report = Report(
                memberId = memberId,
                resource = resource,
                title = "스터디 그룹 신고 제목",
                content = "스터디 그룹 신고",
                status = ProcessStatus.REPORTED
            )

            then("ResourceType이 올바르게 설정된다") {
                report.resource.resourceType shouldBe ResourceType.STUDY_GROUP
            }
        }
    }

    given("다양한 ProcessStatus로 Report 테스트") {
        val memberId = ObjectId()
        val resource = ReportedResource(
            resourceType = ResourceType.STUDY_GROUP,
            resourceId = ObjectId().toHexString()
        )

        `when`("각각의 ProcessStatus로 Report를 생성할 때") {
            then("REPORTED 상태로 생성할 수 있다") {
                val reportedReport = Report(
                    memberId = memberId,
                    resource = resource,
                    title = "신고된 상태 제목",
                    content = "신고된 상태",
                    status = ProcessStatus.REPORTED
                )
                reportedReport.status shouldBe ProcessStatus.REPORTED
            }

            then("PROCESSED 상태로 생성할 수 있다") {
                val processedReport = Report(
                    memberId = memberId,
                    resource = resource,
                    title = "처리된 상태 제목",
                    content = "처리된 상태",
                    status = ProcessStatus.PROCESSED
                )
                processedReport.status shouldBe ProcessStatus.PROCESSED
            }

            then("REJECTED 상태로 생성할 수 있다") {
                val rejectedReport = Report(
                    memberId = memberId,
                    resource = resource,
                    title = "거절된 상태 제목",
                    content = "거절된 상태",
                    status = ProcessStatus.REJECTED
                )
                rejectedReport.status shouldBe ProcessStatus.REJECTED
            }
        }
    }

    /**
     * 테스트용 Report 엔티티를 생성합니다.
     *
     * @param title 신고 제목
     * @param content 신고 내용
     * @param status 처리 상태
     * @return 생성된 Report 엔티티
     */
    fun createTestReport(
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
})
