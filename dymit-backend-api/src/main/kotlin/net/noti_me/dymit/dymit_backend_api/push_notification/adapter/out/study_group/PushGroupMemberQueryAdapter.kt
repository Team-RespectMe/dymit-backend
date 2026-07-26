package net.noti_me.dymit.dymit_backend_api.push_notification.adapter.out.study_group

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.LoadPushGroupMemberPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.dto.PushGroupMemberDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

/**
 * 기존 스터디 그룹 조회 포트를 푸시 모듈의 수신자 조회 포트에 연결합니다.
 */
@Component
class PushGroupMemberQueryAdapter(
    private val studyGroupMemberPort: StudyGroupMemberPort
) : LoadPushGroupMemberPort {

    /**
     * 그룹 회원을 조회해 푸시 소유 DTO로 변환합니다.
     */
    override fun loadByGroupId(groupId: ObjectId): List<PushGroupMemberDto> {
        return studyGroupMemberPort.findByGroupId(groupId)
            .map { PushGroupMemberDto(memberId = it.memberId) }
    }
}
