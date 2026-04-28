package net.noti_me.dymit.dymit_backend_api.controllers.study_group

import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberNicknameChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.member.events.MemberProfileImageChangedEvent
import net.noti_me.dymit.dymit_backend_api.domain.study_group.ProfileImageVo 
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.types.ObjectId
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
class StudyGroupTransactionEventHandler(
    private val groupMemberRepository: StudyGroupMemberRepository,
) {

    private val DEFAULT_BATCH_SIZE = 100

    @Async
    @EventListener
    fun onProfileImageChanged(e: MemberProfileImageChangedEvent) {
        val member = e.member
        var cursor: ObjectId? = null
        do {
            var groupMembers = groupMemberRepository.findByMemberId(
                memberId = member.id!!,
                cursor = cursor,
                limit = DEFAULT_BATCH_SIZE + 1
            )
            var hasNext = false;

            if ( groupMembers.size > DEFAULT_BATCH_SIZE ) {
                groupMembers = groupMembers.take(DEFAULT_BATCH_SIZE).toList()
                cursor = groupMembers.last().id
                hasNext = true
            }
            groupMembers.forEach { groupMember ->
                groupMember.updateProfileImage(
                    ProfileImageVo.from(member.profileImage)
                )
                groupMemberRepository.update(groupMember)
            }
        } while ( hasNext );
    }

    @Async
    @EventListener
    fun onMemberNicknameChanged(e: MemberNicknameChangedEvent) {
        val member = e.member
        var cursor: ObjectId? = null

        do {
            var groupMembers = groupMemberRepository.findByMemberId(
                memberId = member.id!!,
                cursor = cursor,
                limit = DEFAULT_BATCH_SIZE + 1
            )
            var hasNext = false;

            if ( groupMembers.size > DEFAULT_BATCH_SIZE ) {
                groupMembers = groupMembers.take(DEFAULT_BATCH_SIZE).toList()
                cursor = groupMembers.last().id
                hasNext = true
            }
            groupMembers.forEach { groupMember ->
                groupMember.updateNickname(member.nickname)
                groupMemberRepository.update(groupMember)
            }
        } while ( hasNext );
    }
}

