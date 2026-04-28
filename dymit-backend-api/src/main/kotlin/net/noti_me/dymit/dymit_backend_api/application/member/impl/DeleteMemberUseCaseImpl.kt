package net.noti_me.dymit.dymit_backend_api.application.member.impl

import net.noti_me.dymit.dymit_backend_api.application.member.usecases.DeleteMemberUseCase
import net.noti_me.dymit.dymit_backend_api.common.errors.ErrorMessage
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import org.springframework.stereotype.Service
import org.springframework.context.ApplicationEventPublisher

@Service
class DeleteMemberUseCaseImpl(
    private val loadMemberPort: LoadMemberPort,
    private val saveMemberPort: SaveMemberPort,
    private val loadGroupPort: LoadStudyGroupPort,
): DeleteMemberUseCase {

    override fun deleteMember(loginMember: MemberInfo, memberId: String) {
        if (loginMember.memberId != memberId) {
            throw ForbiddenException(message = "접근 권한이 없습니다.")
        }

        val member = loadMemberPort.loadById(memberId)
            ?: return

        val ownedGroups = loadGroupPort.loadByOwnerId(member.identifier)
        if (ownedGroups.isNotEmpty()) {
            throw ForbiddenException(
                code = ErrorMessage.MEMBER_CAN_NOT_LEAVE_OWNED_GROUP.code
                ,message = ErrorMessage.MEMBER_CAN_NOT_LEAVE_OWNED_GROUP.message
            )
        }
        member.leaveService()
        // saveMemberPort.delete(member)
        saveMemberPort.update(member)
    }
}
