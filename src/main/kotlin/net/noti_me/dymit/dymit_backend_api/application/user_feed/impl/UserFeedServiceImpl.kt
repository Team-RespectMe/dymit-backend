package net.noti_me.dymit.dymit_backend_api.application.user_feed.impl

import net.noti_me.dymit.dymit_backend_api.application.user_feed.UserFeedService
import net.noti_me.dymit.dymit_backend_api.application.user_feed.dto.UserFeedDto
import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.errors.NotFoundException
import net.noti_me.dymit.dymit_backend_api.common.security.jwt.MemberInfo
import net.noti_me.dymit.dymit_backend_api.ports.persistence.userFeed.UserFeedRepository
import org.springframework.stereotype.Service

@Service
class UserFeedServiceImpl(
    private val userFeedRepository: UserFeedRepository
) : UserFeedService {

    private val logger = org.slf4j.LoggerFactory.getLogger(javaClass)

    override fun getUserFeeds(memberInfo: MemberInfo, cursorId: String?, size: Int): List<UserFeedDto> {
        logger.debug("Getting UserFeeds for memberId: ${memberInfo.memberId} with cursor: $cursorId and size: $size")
        val userFeeds = userFeedRepository.findByMemberId(
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
}
