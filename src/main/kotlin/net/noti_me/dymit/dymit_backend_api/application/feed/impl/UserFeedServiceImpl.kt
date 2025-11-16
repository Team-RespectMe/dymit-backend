package net.noti_me.dymit.dymit_backend_api.application.feed.impl

import net.noti_me.dymit.dymit_backend_api.application.feed.GroupFeedService
import net.noti_me.dymit.dymit_backend_api.application.feed.UserFeedService
import net.noti_me.dymit.dymit_backend_api.application.feed.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeedQueryHistory
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedQueryHistoryRepository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.user_feed.UserFeedRepository
import org.bson.types.ObjectId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

@Service
class UserFeedServiceImpl(
    private val loadMemberPort: LoadMemberPort,
    private val userFeedRepository: UserFeedRepository,
    private val groupFeedService: GroupFeedService,
    private val studyGroupMemberRepository: StudyGroupMemberRepository,
    private val userFeedQueryHistoryRepository: UserFeedQueryHistoryRepository
) : UserFeedService {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun createUserFeed(userFeed: UserFeed): UserFeed {
        return userFeedRepository.save(userFeed)
    }


    override fun getUserFeeds(memberInfo: MemberInfo, cursorId: String?, size: Int): List<UserFeedDto> {
        logger.debug("Getting UserFeeds for memberId: ${memberInfo.memberId} with cursor: $cursorId and size: $size")

        // 그룹 피드를 먼저 가져와서 영속화 한다.
        pullUnreadGroupFeeds(ObjectId(memberInfo.memberId))

        val userFeeds = userFeedRepository.findByMemberIdOrderByCreatedAtDesc(
            memberId = memberInfo.memberId,
            cursor = cursorId,
            size = size.toLong()
        )
        logger.debug("User Feeds found: ${userFeeds.size}")

        return userFeeds.map { UserFeedDto.from(it) }
    }

    override fun deleteUserFeed(memberInfo: MemberInfo, feedId: String) {
        val userFeed = userFeedRepository.findById(feedId)
            ?: throw NotFoundException("피드를 찾을 수 없습니다.")

        if (!userFeed.isOwnedBy(memberInfo.memberId)) {
            throw ForbiddenException(message="피드 삭제 권한이 없습니다.")
        }

        userFeedRepository.deleteById(feedId)
    }

    override fun markFeedAsRead(memberInfo: MemberInfo, feedId: String) {
        val userFeed = userFeedRepository.findById(feedId)
            ?: throw NotFoundException(message="피드를 찾을 수 없습니다.")

        if (!userFeed.isOwnedBy(memberInfo.memberId)) {
            throw ForbiddenException(message="피드 읽음 처리 권한이 없습니다.")
        }

        userFeed.markAsRead()
        userFeedRepository.save(userFeed)
    }

    private fun pullUnreadGroupFeeds(memberId: ObjectId) {
        val history = userFeedQueryHistoryRepository.findByMemberId(memberId)
            ?: userFeedQueryHistoryRepository.save(
                UserFeedQueryHistory(
                    memberId = memberId,
                    lastFeedId = null
                )
            )

        if ( history.lastFeedId == null ) {
            // 처음 조회하는 경우 마지막 QueryId 가 없으므로, 멤버가 생성된 시점의 가장 오래된 FeedId 로 설정한다.
            val createdAt = loadMemberPort.loadById(memberId)?.createdAt 
                ?: LocalDateTime.now()
            val createdDate = Date.from( createdAt.atZone(ZoneId.systemDefault()).toInstant() )
            val lastFeedId = ObjectId.getSmallestWithDate(createdDate)
            history.updateLastGroupQueryId(lastFeedId)
        }

        val groupIds = studyGroupMemberRepository.findGroupIdsByMemberId(memberId)
            .map { ObjectId(it) }

        var groupFeeds: List<GroupFeed> = emptyList()

        do {
            groupFeeds = groupFeedService.pullUnreadGroupFeeds(
                    groupIds = groupIds,
                    cursor = history.lastFeedId,
                    size = 101)
                .asSequence()
                .take(100)
                .toList()

            val lastId = groupFeeds.lastOrNull()?.id

            val targets = groupFeeds
                .asSequence()
                .map { groupFeed ->
                    UserFeed.create(memberId, groupFeed)
                }
                .sortedByDescending { it.createdAt }
                .toList()

            if ( targets.isEmpty() ) return

            userFeedRepository.saveAll(targets)
            lastId?.let {
                history.updateLastGroupQueryId(lastId)
                userFeedQueryHistoryRepository.save(history)
            }
        } while( groupFeeds.size >= 100 )
    }
}
