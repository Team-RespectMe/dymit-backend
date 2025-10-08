package net.noti_me.dymit.dymit_backend_api.application.feed

import net.noti_me.dymit.dymit_backend_api.application.feed.dto.CreateGroupFeedCommand
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import org.bson.types.ObjectId
import java.time.LocalDateTime

interface GroupFeedService {

    /**
     * 그룹 피드를 생성합니다.
     * @param command 그룹 피드 생성 명령
     * @return 생성된 그룹 피드
     */
    fun createGroupFeed(command: CreateGroupFeedCommand): GroupFeed

    /**
     * 읽지 않은 그룹 피드를 가져옵니다.
     * @param groupIds 그룹 ID 목록
     * @param cursor 페이징을 위한 커서 (마지막으로 읽은 피드의 ID)
     * @param size 가져올 피드의 최대 개수
     * @return 읽지 않은 그룹 피드 목록
     */
    fun pullUnreadGroupFeeds(groupIds: List<ObjectId>, cursor: ObjectId?, size: Long): List<GroupFeed>

    /**
     * 사용자의 마지막 읽은 시간을 업데이트합니다.
     * @param memberId 사용자 ID
     * @param lastReadId 마지막으로 읽은 피드의 ID
     */
    fun updateLastReadAt(memberId: ObjectId, lastReadId: ObjectId)
}