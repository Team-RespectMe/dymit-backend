package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.MarkAllUserFeedsAsReadUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.MarkAllUserFeedsAsReadCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import org.springframework.stereotype.Service

/**
 * 모든 개인 피드 읽음 처리 유스케이스 구현입니다.
 *
 * @param pullUserFeedsService 그룹 피드 반영 서비스
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 */
@Service
class MarkAllUserFeedsAsReadService(
    private val pullUserFeedsService: PullUserFeedsService,
    private val userFeedPersistencePort: UserFeedPersistencePort
) : MarkAllUserFeedsAsReadUseCase {

    /**
     * 미반영 그룹 피드를 먼저 반영한 뒤 읽지 않은 모든 피드를 읽음 처리합니다.
     *
     * @param command 읽음 처리 명령
     */
    override fun execute(command: MarkAllUserFeedsAsReadCommand) {
        pullUserFeedsService.pull(command.memberId)
        userFeedPersistencePort.markAllUnreadAsReadByMemberId(command.memberId)
    }
}
