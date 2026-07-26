package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.QueryStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.StudyRecruitmentDto

/**
 * 스터디 모집 목록 조회 유즈케이스입니다.
 */
interface QueryStudyRecruitmentUseCase {

    /**
     * 커서 기반으로 스터디 모집 목록을 조회합니다.
     *
     * @param command 스터디 모집 목록 조회 명령
     * @return 입력 포트용 스터디 모집 정보 목록
     */
    fun execute(command: QueryStudyRecruitmentCommand): List<StudyRecruitmentDto>
}
