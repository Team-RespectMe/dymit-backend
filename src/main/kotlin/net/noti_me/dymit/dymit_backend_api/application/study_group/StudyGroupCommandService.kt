package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo

interface StudyGroupCommandService {

    /**
     * 스터디 그룹을 생성합니다.
     * @param member 스터디 그룹을 생성하는 멤버 정보
     * @param command 스터디 그룹 생성 커맨드
     */
    fun createStudyGroup(
        member: MemberInfo,
        command: StudyGroupCreateCommand
    ): StudyGroupDto

    /**
     * 스터디 그룹에 참여합니다.
     * @param member 스터디 그룹에 참여할 멤버 정보
     * @param command 스터디 그룹 참여 커맨드
     */
    fun joinStudyGroup(
        member: MemberInfo,
        command: StudyGroupJoinCommand
    ): StudyGroupMemberDto

    /**
     * 스터디 그룹을 삭제합니다.
     * @param member 스터디 그룹을 탈퇴할 멤버 정보
     * @param groupId 탈퇴할 스터디 그룹의 ID
     * @return 삭제 성공 여부
     */
    fun deleteStudyGroup(
        member: MemberInfo,
        groupId: String
    ): Boolean

    fun updateStudyGroupProfileImage(
        member: MemberInfo,
        command: StudyGroupImageUpdateCommand
    ): StudyGroupDto

//    fun updateStudyGroupInfo(
//        member: MemberInfo,
//        command: StudyGroupNicknameUpdateCommand
//    ): StudyGroupDto
}