package net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group

import net.noti_me.dymit.dymit_backend_api.reminder.application.port.`out`.study_group.dto.ReminderStudyGroupDto
import org.bson.types.ObjectId

/**
 * Reminder 모듈이 스터디 그룹 정보를 조회하는 출력 포트입니다.
 */
interface ReminderStudyGroupPort {

    /**
     * 그룹 식별자로 알림에 필요한 그룹 정보를 조회합니다.
     *
     * @param groupId 그룹 식별자
     * @return Reminder 소유 그룹 DTO 또는 null
     */
    fun loadByGroupId(groupId: ObjectId): ReminderStudyGroupDto?
}
