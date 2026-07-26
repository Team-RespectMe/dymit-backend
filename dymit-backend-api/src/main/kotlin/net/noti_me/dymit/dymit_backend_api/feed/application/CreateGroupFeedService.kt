package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.CreateGroupFeedUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.event.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.GroupFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.GroupFeed
import org.bson.types.ObjectId
import org.springframework.stereotype.Service

/**
 * 그룹 피드 생성 유스케이스 구현입니다.
 *
 * @param groupFeedPersistencePort 그룹 피드 영속성 포트
 */
@Service
class CreateGroupFeedService(
    private val groupFeedPersistencePort: GroupFeedPersistencePort
) : CreateGroupFeedUseCase {

    /**
     * 그룹 피드를 생성합니다.
     *
     * @param command 생성 명령
     */
    override fun execute(command: CreateGroupFeedCommand) {
        groupFeedPersistencePort.save(
            GroupFeed(
                groupId = ObjectId(command.groupId),
                iconType = command.iconType,
                eventName = command.eventName,
                title = command.title,
                messages = command.messages,
                associates = command.associates,
                excludedMemberIds = command.excludedMemberIds.mapTo(mutableSetOf(), ::ObjectId)
            )
        )
    }
}
