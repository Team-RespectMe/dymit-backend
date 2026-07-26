package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreatePersonalFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreatePersonalFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 개인 피드 생성 유스케이스 구현입니다.
 *
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 */
@Service
class CreatePersonalFeedService(
    private val userFeedPersistencePort: UserFeedPersistencePort
) : CreatePersonalFeedUseCase {

    /**
     * 개인 피드를 생성합니다.
     *
     * @param command 생성 명령
     */
    override fun execute(command: CreatePersonalFeedCommand) {
        userFeedPersistencePort.save(
            UserFeed(
                memberId = ObjectId(command.memberId),
                iconType = command.iconType,
                eventName = command.eventName,
                messages = command.messages,
                associates = command.associates
            )
        )
    }
}
