package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.BumpStudyRecruitmentCommand
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DymitStudyRecruitmentDto

/**
 * Dymit 스터디 모집글 끌어올리기 유즈케이스입니다.
 */
fun interface BumpStudyRecruitmentUseCase {

    /**
     * 그룹 소유자 권한을 확인하고 모집글을 끌어올립니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param command 모집글 끌어올리기 명령
     * @return 끌어올린 모집글 DTO
     */
    fun execute(
        memberInfo: MemberInfo,
        command: BumpStudyRecruitmentCommand
    ): DymitStudyRecruitmentDto
}
