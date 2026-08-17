package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.UpdateDymitStudyRecruitmentCommand

/**
 * Dymit 스터디 모집글 수정 유즈케이스입니다.
 */
interface UpdateDymitStudyRecruitmentUseCase {

    /**
     * 그룹 소유자 권한을 확인하고 모집글을 수정합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param command 모집글 수정 명령
     * @return 수정된 모집글 DTO
     */
    fun execute(
        memberInfo: MemberInfo,
        command: UpdateDymitStudyRecruitmentCommand
    ): DymitStudyRecruitmentDto
}
