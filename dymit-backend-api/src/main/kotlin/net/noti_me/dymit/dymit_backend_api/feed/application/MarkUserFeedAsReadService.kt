package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.MarkUserFeedAsReadUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.MarkUserFeedAsReadCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import org.springframework.stereotype.Service

/**
 * 개인 피드 읽음 처리 유스케이스 구현입니다.
 *
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 */
@Service
class MarkUserFeedAsReadService(
    private val userFeedPersistencePort: UserFeedPersistencePort
) : MarkUserFeedAsReadUseCase {

    /**
     * 소유권을 확인하고 개인 피드를 읽음 처리합니다.
     *
     * @param command 읽음 처리 명령
     */
    override fun execute(command: MarkUserFeedAsReadCommand) {
        val userFeed = userFeedPersistencePort.findById(command.feedId)
            ?: throw NotFoundException(message = "피드를 찾을 수 없습니다.")

        if (!userFeed.isOwnedBy(command.memberId)) {
            throw ForbiddenException(message = "피드 읽음 처리 권한이 없습니다.")
        }

        userFeed.markAsRead()
        userFeedPersistencePort.save(userFeed)
    }
}
