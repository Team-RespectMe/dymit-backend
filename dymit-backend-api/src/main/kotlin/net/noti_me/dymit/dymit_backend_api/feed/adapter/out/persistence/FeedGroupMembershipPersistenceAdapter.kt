package net.noti_me.dymit.dymit_backend_api.feed.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.LoadFeedGroupMembershipPort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedGroupMembershipDto
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.StudyGroupMemberPort
import org.bson.types.ObjectId
import org.springframework.stereotype.Component

/**
 * StudyGroup 모듈의 조회 포트를 Feed 소유 포트로 변환하는 출력 어댑터입니다.
 *
 * @param studyGroupMemberPort StudyGroup 모듈 조회 포트
 */
@Component
class FeedGroupMembershipPersistenceAdapter(
    private val studyGroupMemberPort: StudyGroupMemberPort
) : LoadFeedGroupMembershipPort {

    /**
     * 회원의 그룹 식별자를 Feed 소유 데이터로 변환합니다.
     *
     * @param memberId 회원 식별자
     * @return Feed 소유 그룹 가입 데이터
     */
    override fun loadByMemberId(memberId: String): FeedGroupMembershipDto {
        return FeedGroupMembershipDto(
            groupIds = studyGroupMemberPort.findGroupIdsByMemberId(ObjectId(memberId))
        )
    }
}
