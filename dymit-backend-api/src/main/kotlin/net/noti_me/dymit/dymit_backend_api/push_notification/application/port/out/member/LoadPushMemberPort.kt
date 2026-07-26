package net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member

import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.dto.PushMemberDto
import org.bson.types.ObjectId

/**
 * 푸시 수신 회원 정보를 조회하는 출력 포트입니다.
 */
interface LoadPushMemberPort {

    /**
     * 회원 ID로 푸시 수신 정보를 조회합니다.
     */
    fun loadById(memberId: ObjectId): PushMemberDto?

    /**
     * 여러 회원 ID로 푸시 수신 정보를 조회합니다.
     */
    fun loadByIds(memberIds: List<ObjectId>): List<PushMemberDto>
}
