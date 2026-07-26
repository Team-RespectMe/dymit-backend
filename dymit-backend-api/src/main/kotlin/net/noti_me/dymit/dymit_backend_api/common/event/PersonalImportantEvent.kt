package net.noti_me.dymit.dymit_backend_api.common.event

import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import org.springframework.context.ApplicationEvent

/**
 * 개인 피드와 푸시를 함께 전달하는 중요 이벤트입니다.
 *
 * @param source 이벤트 원본
 */
abstract class PersonalImportantEvent(
    source: Any
) : ApplicationEvent(source), Pushable, PersonalFeedEvent {

    /**
     * 개인 피드 이벤트 데이터를 생성합니다.
     *
     * @return 개인 피드 데이터 목록
     */
    protected abstract fun processPersonalFeedData(): List<PersonalFeedEventData>

    /**
     * 개인 푸시 메시지를 생성합니다.
     *
     * @return 개인 푸시 메시지
     */
    protected abstract fun processPushMessage(): PersonalPushMessage

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
     * @return 개인 푸시 메시지
     */
    final override fun toPushMessage(): PersonalPushMessage {
        return processPushMessage()
    }
}
