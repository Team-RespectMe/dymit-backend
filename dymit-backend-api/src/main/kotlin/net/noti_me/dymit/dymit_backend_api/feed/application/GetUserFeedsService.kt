package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.GetUserFeedsUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.AssociatedResourceDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.FeedMessageDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUserFeedsCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * 개인 피드 목록 조회 유스케이스 구현입니다.
 *
 * @param pullUserFeedsService 그룹 피드 반영 서비스
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 */
@Service
class GetUserFeedsService(
    private val pullUserFeedsService: PullUserFeedsService,
    private val userFeedPersistencePort: UserFeedPersistencePort
) : GetUserFeedsUseCase {

    /**
     * 미반영 그룹 피드를 개인 피드로 변환한 뒤 목록을 조회합니다.
     *
     * @param command 조회 명령
     * @return 개인 피드 목록
     */
    override fun execute(command: GetUserFeedsCommand): List<UserFeedDto> {
        pullUserFeedsService.pull(command.memberId)

        return userFeedPersistencePort.findByMemberIdOrderByCreatedAtDesc(
            memberId = command.memberId,
            cursor = command.cursorId,
            size = command.size.toLong()
        ).map(::toDto)
    }

    private fun toDto(userFeed: UserFeed): UserFeedDto {
        return UserFeedDto(
            id = userFeed.identifier,
            memberId = userFeed.memberId.toHexString(),
            iconType = userFeed.iconType,
            eventName = userFeed.eventName,
            messages = userFeed.messages.map {
                FeedMessageDto(
                    text = it.text,
                    textColor = it.textColor,
                    highlightColor = it.highlightColor
                )
            },
            associates = userFeed.associates.map {
                AssociatedResourceDto(
                    type = it.type,
                    resourceId = it.resourceId
                )
            },
            createdAt = userFeed.createdAt ?: Instant.now(),
            isRead = userFeed.isRead
        )
    }
}
