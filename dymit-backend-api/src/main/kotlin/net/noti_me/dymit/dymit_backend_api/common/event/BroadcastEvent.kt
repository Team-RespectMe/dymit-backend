package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import org.bson.types.ObjectId
import org.springframework.context.ApplicationEvent

/**
 * 여러 회원에게 피드와 푸시를 함께 전달하는 이벤트입니다.
 *
 * @param memberIds 수신 회원 식별자 목록
 */
abstract class BroadcastEvent(
    val memberIds: List<ObjectId>,
) : ApplicationEvent(memberIds), PersonalFeedEvent, BroadcastPushable {

    /**
     * 개인 피드 데이터를 반환합니다.
     *
     * @return 개인 피드 데이터 목록
     */
    final override fun toPersonalFeedData(): List<PersonalFeedEventData> {
        return processPersonalFeedData()
    }

    /**
     * 개인 푸시 메시지를 반환합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    final override fun toPushMessages(): List<PersonalPushMessage> {
        return processPushMessages()
    }

    /**
     * 개인 푸시 메시지를 생성합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    protected abstract fun processPushMessages(): List<PersonalPushMessage>

    /**
     * 개인 피드 이벤트 데이터를 생성합니다.
     *
     * @return 개인 피드 데이터 목록
     */
    protected abstract fun processPersonalFeedData(): List<PersonalFeedEventData>
}
