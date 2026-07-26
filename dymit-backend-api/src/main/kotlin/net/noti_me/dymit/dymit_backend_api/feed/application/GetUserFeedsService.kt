package net.noti_me.dymit.dymit_backend_api.feed.application

import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.GetUserFeedsUseCase
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.AssociatedResourceDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.FeedMessageDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.GetUserFeedsCommand
import net.noti_me.dymit.dymit_backend_api.feed.application.port.`in`.web.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.GroupFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.LoadFeedGroupMembershipPort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.LoadFeedMemberPort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.UserFeedQueryHistoryPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.GroupFeed
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeed
import net.noti_me.dymit.dymit_backend_api.feed.domain.UserFeedQueryHistory
import org.bson.types.ObjectId
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * 개인 피드 목록 조회 유스케이스 구현입니다.
 *
 * @param loadFeedMemberPort Feed 회원 조회 포트
 * @param loadFeedGroupMembershipPort Feed 그룹 가입 조회 포트
 * @param userFeedPersistencePort 개인 피드 영속성 포트
 * @param groupFeedPersistencePort 그룹 피드 영속성 포트
 * @param queryHistoryPersistencePort 조회 이력 영속성 포트
 */
@Service
class GetUserFeedsService(
    private val loadFeedMemberPort: LoadFeedMemberPort,
    private val loadFeedGroupMembershipPort: LoadFeedGroupMembershipPort,
    private val userFeedPersistencePort: UserFeedPersistencePort,
    private val groupFeedPersistencePort: GroupFeedPersistencePort,
    private val queryHistoryPersistencePort: UserFeedQueryHistoryPersistencePort
) : GetUserFeedsUseCase {

    /**
     * 미반영 그룹 피드를 개인 피드로 변환한 뒤 목록을 조회합니다.
     *
     * @param command 조회 명령
     * @return 개인 피드 목록
     */
    override fun execute(command: GetUserFeedsCommand): List<UserFeedDto> {
        pullUnreadGroupFeeds(ObjectId(command.memberId))

        return userFeedPersistencePort.findByMemberIdOrderByCreatedAtDesc(
            memberId = command.memberId,
            cursor = command.cursorId,
            size = command.size.toLong()
        ).map(::toDto)
    }

    private fun pullUnreadGroupFeeds(memberId: ObjectId) {
        val history = queryHistoryPersistencePort.findByMemberId(memberId)
            ?: queryHistoryPersistencePort.save(
                UserFeedQueryHistory(
                    memberId = memberId,
                    lastFeedId = null
                )
            )

        if (history.lastFeedId == null) {
            val createdAt = loadFeedMemberPort.loadById(memberId.toHexString())?.createdAt
                ?: LocalDateTime.now()
            val createdDate = Date.from(createdAt.atZone(ZoneId.systemDefault()).toInstant())
            history.updateLastGroupQueryId(ObjectId.getSmallestWithDate(createdDate))
            queryHistoryPersistencePort.save(history)
        }

        val groupIds = loadFeedGroupMembershipPort.loadByMemberId(memberId.toHexString())
            .groupIds
            .map(::ObjectId)

        do {
            var groupFeeds = groupFeedPersistencePort.findByGroupIdsOrderByIdDesc(
                groupIds = groupIds,
                cursor = history.lastFeedId,
                size = DEFAULT_BATCH_SIZE + 1L
            )
            val hasNext = groupFeeds.size > DEFAULT_BATCH_SIZE
            groupFeeds = groupFeeds.take(DEFAULT_BATCH_SIZE.toInt())
            val firstId = groupFeeds.firstOrNull()?.id

            val targets = groupFeeds
                .asSequence()
                .filterNot { groupFeed -> groupFeed.excludedMemberIds.contains(memberId) }
                .map { groupFeed -> UserFeed.create(memberId, groupFeed) }
                .sortedByDescending { it.createdAt }
                .toList()

            if (targets.isEmpty()) return

            userFeedPersistencePort.saveAll(targets)

            if (history.lastFeedId!! < firstId) {
                history.updateLastGroupQueryId(firstId!!)
                queryHistoryPersistencePort.save(history)
            }
        } while (hasNext)
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
            createdAt = userFeed.createdAt ?: LocalDateTime.now(),
            isRead = userFeed.isRead
        )
    }

    private companion object {
        const val DEFAULT_BATCH_SIZE: Long = 100L
    }
}
