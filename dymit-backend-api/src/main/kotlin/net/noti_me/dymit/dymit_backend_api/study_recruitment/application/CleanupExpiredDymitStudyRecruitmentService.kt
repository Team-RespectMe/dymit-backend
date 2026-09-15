package net.noti_me.dymit.dymit_backend_api.study_recruitment.application

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.CleanupExpiredDymitStudyRecruitmentUseCase
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CleanupExpiredDymitStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.DeleteExpiredDymitStudyRecruitmentPort
import org.springframework.stereotype.Service

/**
 * 만료된 Dymit 스터디 모집글 정리 서비스입니다.
 *
 * @property deleteExpiredRecruitmentPort 만료 모집글 물리 삭제 출력 포트
 */
@Service
class CleanupExpiredDymitStudyRecruitmentService(
    private val deleteExpiredRecruitmentPort: DeleteExpiredDymitStudyRecruitmentPort
) : CleanupExpiredDymitStudyRecruitmentUseCase {

    /**
     * 기준 시각 이전에 수정된 모집 종료 글을 물리 삭제합니다.
     *
     * @param command 만료 모집글 정리 명령
     */
    override fun execute(command: CleanupExpiredDymitStudyRecruitmentCommand) {
        deleteExpiredRecruitmentPort.deleteExpired(command.cutoff)
    }
}
