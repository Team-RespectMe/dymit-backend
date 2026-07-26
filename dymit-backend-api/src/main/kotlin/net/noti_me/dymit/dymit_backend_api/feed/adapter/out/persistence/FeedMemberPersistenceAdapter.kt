package net.noti_me.dymit.dymit_backend_api.feed.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.LoadFeedMemberPort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.dto.FeedMemberDto
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import org.springframework.stereotype.Component

/**
 * Member 모듈의 조회 포트를 Feed 소유 포트로 변환하는 출력 어댑터입니다.
 *
 * @param loadMemberPort Member 모듈 조회 포트
 */
@Component
class FeedMemberPersistenceAdapter(
    private val loadMemberPort: LoadMemberPort
) : LoadFeedMemberPort {

    /**
     * 회원 가입 시각만 Feed 소유 데이터로 변환합니다.
     *
     * @param memberId 회원 식별자
     * @return Feed 소유 회원 데이터 또는 null
     */
    override fun loadById(memberId: String): FeedMemberDto? {
        return loadMemberPort.loadById(memberId)?.let {
            FeedMemberDto(createdAt = it.createdAt ?: return null)
        }
    }
}
