package net.noti_me.dymit.dymit_backend_api.units.study_recruitment

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import net.noti_me.dymit.dymit_backend_api.configs.QuartzConfig
import net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.quartz.DymitStudyRecruitmentCleanupJob
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.CleanupExpiredDymitStudyRecruitmentService
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CleanupExpiredDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CleanupExpiredDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.DeleteExpiredDymitStudyRecruitmentPort
import org.quartz.CronTrigger
import org.quartz.JobExecutionContext
import java.time.Duration
import java.time.Instant

internal class CleanupExpiredDymitStudyRecruitmentTest : BehaviorSpec() {

    init {
        Given("만료 모집글 정리 서비스") {
            val port = mockk<DeleteExpiredDymitStudyRecruitmentPort>()
            val service = CleanupExpiredDymitStudyRecruitmentService(port)
            val cutoff = Instant.parse("2026-09-14T03:00:00Z")
            every { port.deleteExpired(cutoff) } returns 0L

            When("정리 명령을 실행하면") {
                service.execute(CleanupExpiredDymitStudyRecruitmentCommand(cutoff))

                Then("cutoff를 삭제 포트에 그대로 전달한다") {
                    verify(exactly = 1) { port.deleteExpired(cutoff) }
                }
            }
        }

        Given("정시 만료 모집글 정리 Quartz 작업") {
            val useCase = mockk<CleanupExpiredDymitStudyRecruitmentUseCase>()
            val command = slot<CleanupExpiredDymitStudyRecruitmentCommand>()
            every { useCase.execute(capture(command)) } returns Unit

            When("작업을 실행하면") {
                val before = Instant.now()
                DymitStudyRecruitmentCleanupJob(useCase).execute(mockk<JobExecutionContext>(relaxed = true))
                val after = Instant.now()

                Then("실행 시점보다 24시간 이전인 cutoff를 전달한다") {
                    (command.captured.cutoff >= before.minus(Duration.ofHours(24))) shouldBe true
                    (command.captured.cutoff <= after.minus(Duration.ofHours(24))) shouldBe true
                }
            }
        }

        Given("만료 모집글 정리 Quartz 트리거") {
            val config = QuartzConfig()
            val jobDetail = config.dymitStudyRecruitmentCleanupJobDetail()

            When("트리거를 생성하면") {
                val trigger = config.dymitStudyRecruitmentCleanupTrigger(jobDetail) as CronTrigger

                Then("서울 시간 기준 매시 0분에 실행되도록 설정한다") {
                    trigger.cronExpression shouldBe "0 0 * * * ?"
                    trigger.timeZone.id shouldBe "Asia/Seoul"
                }
            }
        }
    }
}
