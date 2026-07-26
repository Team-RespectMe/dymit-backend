package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.dto

import org.bson.types.ObjectId

/**
 * 푸시 전송에 필요한 회원 정보입니다.
 */
data class PushMemberDto(
    val id: ObjectId,
    val deviceTokens: List<PushDeviceTokenDto>
)

/**
 * 푸시 전송에 필요한 디바이스 토큰 정보입니다.
 */
data class PushDeviceTokenDto(
    val token: String,
    val isActive: Boolean
)
