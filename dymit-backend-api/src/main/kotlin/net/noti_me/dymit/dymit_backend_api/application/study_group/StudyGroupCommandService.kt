package net.noti_me.dymit.dymit_backend_api.application.study_group

import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.ChangeGroupOwnerCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.EnlistBlacklistCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupImageUpdateCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupJoinCommand
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command.StudyGroupModifyCommand
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

    fun leaveStudyGroup(
        member: MemberInfo,
        groupId: String
    ): Unit

    /**
     * 스터디 그룹 멤버를 강퇴합니다.
     * @param loginMember 강퇴를 수행하는 멤버 정보 (스터디 그룹장이어야 함)
     * @param groupId 강퇴할 스터디 그룹의 ID
     * @param targetMemberId 강퇴할 멤버의 ID
     */
    fun expelStudyGroupMember(
        loginMember: MemberInfo,
        groupId: String,
        targetMemberId: String
    ): Unit

    /**
     * 특정 멤버를 블랙리스트로 등록합니다.
     * @param loginMember 블랙리스트 등록을 수행하는 멤버 정보 (스터디 그룹장이어야 함)
     * @param command 블랙리스트 등록 커맨드
     */
    fun enlistBlacklist(
        loginMember: MemberInfo,
        command: EnlistBlacklistCommand
    ): Unit

    /**
     * 특정 멤버를 블랙리스트에서 해제합니다.
     * @param loginMember 블랙리스트 해제를 수행하는 멤버 정보 (스터디 그룹장이어야 함)
     * @param groupId 블랙리스트에서 해제할 스터디 그룹의 ID
     * @param targetMemberId 블랙리스트에서 해제할 멤버의 ID
     */
    fun delistBlacklist(
        loginMember: MemberInfo,
        groupId: String,
        targetMemberId: String
    ): Unit

    /**
     * 스터디 그룹 소유자를 변경합니다.
     * @param loginMember 소유자 변경을 수행하는 멤버 정보 (현재 스터디 그룹장이어야 함)
     * @param command 소유자 변경 커맨드
     * @return 변경된 스터디 그룹 정보
     */
    fun changeStudyGroupOwner(
        loginMember: MemberInfo,
        command: ChangeGroupOwnerCommand
    ): StudyGroupDto


    fun updateStudyGroupInfo(
        member: MemberInfo,
        command: StudyGroupModifyCommand
    ): StudyGroupDto
}