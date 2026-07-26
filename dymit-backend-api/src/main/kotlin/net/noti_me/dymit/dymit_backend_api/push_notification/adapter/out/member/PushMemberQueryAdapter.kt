package net.noti_me.dymit.dymit_backend_api.push_notification.adapter.out.member

import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.LoadPushMemberPort
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.dto.PushDeviceTokenDto
import net.noti_me.dymit.dymit_backend_api.push_notification.application.port.out.member.dto.PushMemberDto
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

/**
 * 기존 회원 저장소를 푸시 모듈의 회원 조회 포트에 연결합니다.
 */
@Component
class PushMemberQueryAdapter(
    private val loadMemberPort: LoadMemberPort
) : LoadPushMemberPort {

    /**
     * 회원 한 명의 푸시 수신 정보를 조회해 푸시 소유 DTO로 변환합니다.
     */
    override fun loadById(memberId: ObjectId): PushMemberDto? {
        return loadMemberPort.loadById(memberId)?.toPushMemberDto()
    }

    /**
     * 여러 회원의 푸시 수신 정보를 조회해 푸시 소유 DTO로 변환합니다.
     */
    override fun loadByIds(memberIds: List<ObjectId>): List<PushMemberDto> {
        return loadMemberPort.loadByIds(memberIds.map(ObjectId::toHexString))
            .map { it.toPushMemberDto() }
    }

    private fun Member.toPushMemberDto(): PushMemberDto {
        return PushMemberDto(
            id = id!!,
            deviceTokens = deviceTokens.map {
                PushDeviceTokenDto(
                    token = it.token,
                    isActive = it.isActive
                )
            }
        )
    }
}
