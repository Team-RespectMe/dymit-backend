package net.noti_me.dymit.dymit_backend_api.application.task.usecases

import net.noti_me.dymit.dymit_backend_api.application.task.dto.CreateTaskCommand
import net.noti_me.dymit.dymit_backend_api.application.task.dto.TaskDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

/**
 * 과제 생성 유즈케이스입니다.
 */
interface CreateTaskUseCase {

    /**
     * 과제를 생성합니다.
     *
     * @param memberInfo 요청 회원 정보
     * @param groupId 스터디 그룹 ID
     * @param command 과제 생성 명령 DTO
     * @return 생성된 과제 DTO
     */
    fun createTask(memberInfo: MemberInfo, groupId: String, command: CreateTaskCommand): TaskDto
}
