package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.dto.PushGroupMemberDto
import org.bson.types.ObjectId

/**
 * 그룹 푸시 수신자를 조회하는 출력 포트입니다.
 */
interface LoadPushGroupMemberPort {

    /**
     * 그룹에 소속된 모든 회원 식별자를 조회합니다.
     */
    fun loadByGroupId(groupId: ObjectId): List<PushGroupMemberDto>
}
