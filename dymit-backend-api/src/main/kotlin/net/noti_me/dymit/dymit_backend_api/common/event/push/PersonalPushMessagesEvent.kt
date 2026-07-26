package net.noti_me.dymit.dymit_backend_api.common.event.push

import org.bson.types.ObjectId

/**
 * 모듈 독립적인 개인 푸시 메시지 목록을 제공하는 이벤트 계약입니다.
 */
interface PersonalPushMessagesEvent {

    /**
     * 수신자별 개인 푸시 메시지를 반환합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    fun toPersonalPushMessages(): List<PersonalPushEventData>
}

/**
 * 개인 푸시 전송에 필요한 모듈 독립 데이터입니다.
 *
 * @param memberId 수신 회원 식별자
 * @param eventName 이벤트 이름
 * @param title 알림 제목
 * @param body 알림 본문
 * @param image 알림 이미지 URL
 * @param data 부가 데이터
 */
data class PersonalPushEventData(
    val memberId: ObjectId,
    val eventName: String,
    val title: String = "Dymit",
    val body: String,
    val image: String?,
    val data: Map<String, String>
)
