package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CleanupExpiredDymitStudyRecruitmentCommand

/**
 * 만료된 Dymit 스터디 모집글을 정리하는 유즈케이스입니다.
 */
fun interface CleanupExpiredDymitStudyRecruitmentUseCase {

    /**
     * 주어진 기준 시각까지 24시간이 지난 모집 종료 글을 정리합니다.
     *
     * @param command 만료 모집글 정리 명령
     */
    fun execute(command: CleanupExpiredDymitStudyRecruitmentCommand)
}
