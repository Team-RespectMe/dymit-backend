package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto.DeleteDymitStudyRecruitmentCommand

/**
 * Dymit 스터디 모집글 삭제 유즈케이스입니다.
 */
interface DeleteDymitStudyRecruitmentUseCase {

    /**
     * 그룹 소유자 권한을 확인하고 모집글을 삭제 상태로 변경합니다.
     *
     * @param memberInfo 로그인 회원 정보
     * @param command 모집글 삭제 명령
     */
    fun execute(
        memberInfo: MemberInfo,
        command: DeleteDymitStudyRecruitmentCommand
    )
}
