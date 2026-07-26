package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.DeleteUserFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.DeleteUserFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import org.springframework.stereotype.Service

/**
 * 개인 피드 삭제 유스케이스 구현입니다.
 *
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 */
@Service
class DeleteUserFeedService(
    private val userFeedPersistencePort: UserFeedPersistencePort
) : DeleteUserFeedUseCase {

    /**
     * 소유권을 확인하고 개인 피드를 삭제합니다.
     *
     * @param command 삭제 명령
     */
    override fun execute(command: DeleteUserFeedCommand) {
        val userFeed = userFeedPersistencePort.findById(command.feedId)
            ?: throw NotFoundException("피드를 찾을 수 없습니다.")

        if (!userFeed.isOwnedBy(command.memberId)) {
            throw ForbiddenException(message = "피드 삭제 권한이 없습니다.")
        }

        userFeedPersistencePort.deleteById(command.feedId)
    }
}
