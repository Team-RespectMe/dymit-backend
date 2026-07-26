package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.common.event.feed.GroupFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.GroupFeedEventData
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.GroupPushMessage
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent

/**
 * 그룹 피드와 푸시를 함께 전달하는 중요 이벤트입니다.
 *
 * @param source 이벤트 원본
 */
abstract class GroupImportantEvent(source: Any)
: ApplicationEvent(source), GroupPushable, GroupFeedEvent {

    private val excludedMemberIds = mutableSetOf<String>()

    /**
     * 그룹 피드 이벤트 데이터를 생성합니다.
     *
     * @return 그룹 피드 데이터
     */
    protected abstract fun processGroupFeedData(): GroupFeedEventData

    /**
     * 그룹 푸시 메시지를 생성합니다.
     *
     * @return 그룹 푸시 메시지
     */
    protected abstract fun processGroupPush(): GroupPushMessage

    /**
     * 제외 회원을 반영한 그룹 피드 데이터를 반환합니다.
     *
     * @return 그룹 피드 데이터
     */
    final override fun toGroupFeedData(): GroupFeedEventData {
        val feed = processGroupFeedData()
        return feed.copy(excludedMemberIds = feed.excludedMemberIds + excludedMemberIds)
    }

    /**
     * 제외 회원을 반영한 그룹 푸시 메시지를 반환합니다.
     *
     * @return 그룹 푸시 메시지
     */
    final override fun toGroupPush(): GroupPushMessage {
        if (excludedMemberIds.isNotEmpty()) {
            val push = processGroupPush()
            push.excluded.addAll(excludedMemberIds.map(::ObjectId))
            return push
        }
        return processGroupPush()
    }

    /**
     * 피드와 푸시에서 제외할 회원을 추가합니다.
     *
     * @param memberId 제외 회원 식별자
     */
    fun addExcludedMemberId(memberId: ObjectId) {
        excludedMemberIds.add(memberId.toHexString())
    }

    /**
     * 주어진 회원 목록에서 제외 회원을 제거합니다.
     *
     * @param memberIds 필터링할 회원 목록
     * @return 필터링된 회원 목록
     */
    fun filterExcludedMemberIds(memberIds: MutableSet<ObjectId>): MutableSet<ObjectId> {
        memberIds.removeAll(excludedMemberIds.map(::ObjectId).toSet())
        return memberIds
    }
}
