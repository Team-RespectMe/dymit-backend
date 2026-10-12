package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.GetUnreadUserFeedCountUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUnreadUserFeedCountCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import org.springframework.stereotype.Service

/**
 * 읽지 않은 개인 피드 수 조회 유스케이스 구현입니다.
 *
 * @param pullUserFeedsService 그룹 피드 반영 서비스
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 */
@Service
class GetUnreadUserFeedCountService(
    private val pullUserFeedsService: PullUserFeedsService,
    private val userFeedPersistencePort: UserFeedPersistencePort
) : GetUnreadUserFeedCountUseCase {

    /**
     * 미반영 그룹 피드를 먼저 반영한 뒤 읽지 않은 피드 수를 조회합니다.
     *
     * @param command 조회 명령
     * @return 읽지 않은 피드 수
     */
    override fun execute(command: GetUnreadUserFeedCountCommand): Long {
        pullUserFeedsService.pull(command.memberId)
        return userFeedPersistencePort.countUnreadByMemberId(command.memberId)
    }
}
