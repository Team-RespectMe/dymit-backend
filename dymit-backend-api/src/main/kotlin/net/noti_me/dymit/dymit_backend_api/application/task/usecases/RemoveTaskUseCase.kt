package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 삭제 유즈케이스입니다.
 */
interface RemoveTaskUseCase {

    /**
     * 과제를 삭제합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param taskId 과제 ID
     */
    fun removeTask(memberInfo: MemberInfo, groupId: String, taskId: String)
}
