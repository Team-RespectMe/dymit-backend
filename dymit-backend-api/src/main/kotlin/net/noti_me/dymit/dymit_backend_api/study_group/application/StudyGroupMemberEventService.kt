package net.noti_me.dymit.dymit_backend_api.study_group.application

import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.study_group.application.dto.command.StudyGroupCreateCommand
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberEventPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberEventDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.LoadStudyGroupPort
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.study_group.domain.ProfileImageVo
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_group.domain.events.GroupOwnerMissingEvent
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class StudyGroupMemberEventService(
    private val loadStudyGroupPort: LoadStudyGroupPort,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val studyGroupCommandService: StudyGroupCommandService,
    private val eventPublisher: ApplicationEventPublisher
) : StudyGroupMemberEventPort {

    override fun memberCreated(member: StudyGroupMemberEventDto) {
        studyGroupCommandService.createStudyGroup(
            member = member.toMemberInfo(),
            command = StudyGroupCreateCommand(
                name = "${member.nickname}님의 스터디 그룹",
                description = "자동 생성된 스터디 그룹입니다."
            )
        )
    }

    override fun memberDeleted(member: StudyGroupMemberEventDto) {
        var cursor: ObjectId? = null
        do {
            val groupMembers = studyGroupMemberRepository.findByMemberId(
                memberId = ObjectId(member.memberId),
                cursor = cursor,
                limit = FETCH_GROUP_MEMBER_LIMIT + 1
            )
            groupMembers.forEach { groupMember ->
                studyGroupCommandService.leaveStudyGroup(
                    member = member.toMemberInfo(),
                    groupId = groupMember.groupId.toHexString()
                )
            }
            cursor = if (groupMembers.size > FETCH_GROUP_MEMBER_LIMIT) {
                groupMembers.last().id
            } else {
                null
            }
        } while (cursor != null)
    }

    override fun memberForceDeleted(memberId: String) {
        loadStudyGroupPort.loadByOwnerId(memberId).forEach { group ->
            eventPublisher.publishEvent(GroupOwnerMissingEvent(group))
        }
    }

    override fun memberNicknameChanged(memberId: String, nickname: String) {
        forEachMembership(memberId) { membership ->
            membership.updateNickname(nickname)
            studyGroupMemberRepository.update(membership)
        }
    }

    override fun memberProfileImageChanged(member: StudyGroupMemberEventDto) {
        val profileImage = ProfileImageVo.of(
            type = member.profileImageType,
            url = member.profileImageUrl
        )
        forEachMembership(member.memberId) { membership ->
            membership.updateProfileImage(profileImage)
            studyGroupMemberRepository.update(membership)
        }
    }

    private fun forEachMembership(
        memberId: String,
        update: (StudyGroupMember) -> Unit
    ) {
        var cursor: ObjectId? = null
        do {
            val result = studyGroupMemberRepository.findByMemberId(
                memberId = ObjectId(memberId),
                cursor = cursor,
                limit = UPDATE_BATCH_SIZE + 1
            )
            val targets = result.take(UPDATE_BATCH_SIZE)
            targets.forEach(update)
            cursor = if (result.size > UPDATE_BATCH_SIZE) targets.lastOrNull()?.id else null
        } while (cursor != null)
    }

    private fun StudyGroupMemberEventDto.toMemberInfo() =
        MemberInfo.of(memberId, nickname, roles)

    companion object {
        private const val FETCH_GROUP_MEMBER_LIMIT = 100
        private const val UPDATE_BATCH_SIZE = 100
    }
}
