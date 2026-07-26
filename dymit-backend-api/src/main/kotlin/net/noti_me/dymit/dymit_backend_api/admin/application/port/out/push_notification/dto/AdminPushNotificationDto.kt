package net.noti_me.dymit.dymit_backend_api.admin.application.port.`out`.push_notification.dto

import org.bson.types.ObjectId

/**
 * 관리자 모듈에서 개인 푸시 전송에 사용하는 데이터입니다.
 *
 * @param memberId 수신 회원 식별자
 * @param title 알림 제목
 * @param body 알림 본문
 * @param eventName 이벤트 이름
 * @param data 부가 데이터
 * @param image 이미지 URL
 */
data class AdminPushNotificationDto(
    val memberId: ObjectId,
    val title: String,
    val body: String,
    val eventName: String,
    val data: Map<String, String>,
    val image: String?
)
