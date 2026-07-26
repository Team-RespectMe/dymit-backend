package net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.dto

import org.bson.types.ObjectId

/**
 * Reminder가 일정 알림을 생성할 때 사용하는 그룹 정보입니다.
 *
 * @param id 그룹 식별자
 * @param ownerId 그룹 소유자 식별자
 * @param name 그룹 이름
 * @param profileImageThumbnail 그룹 썸네일 URL
 */
data class ReminderStudyGroupDto(
    val id: ObjectId,
    val ownerId: ObjectId,
    val name: String,
    val profileImageThumbnail: String
)
