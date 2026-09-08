package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.CheckDymitStudyRecruitmentExistenceCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentExistenceDto

/**
 * 그룹별 Dymit 스터디 모집글 존재 여부 확인 유즈케이스입니다.
 */
interface CheckDymitStudyRecruitmentExistenceUseCase {

    /**
     * 요청한 그룹별 모집글 존재 여부를 확인합니다.
     *
     * @param command 그룹 식별자 목록 확인 명령
     * @return 그룹별 모집글 존재 여부 목록
     */
    fun execute(
        command: CheckDymitStudyRecruitmentExistenceCommand
    ): List<DymitStudyRecruitmentExistenceDto>
}
