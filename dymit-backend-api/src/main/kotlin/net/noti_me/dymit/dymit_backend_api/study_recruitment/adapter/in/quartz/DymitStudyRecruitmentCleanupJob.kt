package net.noti_me.dymit.dymit_backend_api.study_recruitment.adapter.`in`.quartz

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CleanupExpiredDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CleanupExpiredDymitStudyRecruitmentCommand
import org.quartz.DisallowConcurrentExecution
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant

/**
 * 만료된 Dymit 스터디 모집글을 정리하는 Quartz 작업입니다.
 *
 * @property useCase 만료 모집글 정리 유즈케이스
 */
@Component
@DisallowConcurrentExecution
class DymitStudyRecruitmentCleanupJob(
    private val useCase: CleanupExpiredDymitStudyRecruitmentUseCase
) : Job {

    /**
     * 실행 시각으로부터 24시간이 지난 모집글을 정리합니다.
     *
     * @param context Quartz 작업 실행 정보
     */
    override fun execute(context: JobExecutionContext?) {
        useCase.execute(
            CleanupExpiredDymitStudyRecruitmentCommand(
                cutoff = Instant.now().minus(EXPIRATION_PERIOD)
            )
        )
    }

    private companion object {
        val EXPIRATION_PERIOD: Duration = Duration.ofHours(24)
    }
}
