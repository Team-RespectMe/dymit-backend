package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.study_group.dto

import org.bson.types.ObjectId

/**
 * 그룹 푸시 수신자 식별 정보입니다.
 */
data class PushGroupMemberDto(
    val memberId: ObjectId
)
