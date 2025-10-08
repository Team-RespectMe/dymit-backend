package net.noti_me.dymit.dymit_backend_api.domain.study_group.events

import net.noti_me.dymit.dymit_backend_api.common.event.GroupImportantEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.GroupPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.schedule.StudySchedule
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.GroupFeed
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType

/**
 * 스터디 그룹 일정이 생성되는 경우 발행해야하는 이벤트
 * 이 이벤트를 처리하여 다음 두 프로세스로 파생되어야 한다.
 * 1. 그룹 피드
 * 2. 그룹 멤버들에게 푸시 알림
 * @param studySchedule 생성된 스터디 일정
 */
class StudyScheduleCreatedEvent(
    val group: StudyGroup,
    val studySchedule: StudySchedule,
): GroupImportantEvent(studySchedule) {

    override fun processGroupFeed(): GroupFeed {
        return GroupFeed(
            groupId = studySchedule.groupId,
            iconType = IconType.CALENDAR,
            messages = listOf(
                FeedMessage(
                    text = "${group.name} ${studySchedule.session}회차 일정이 추가되었어요!",
                ),
            ),
            associates = listOf(
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP,
                    resourceId = group.identifier
                ),
                AssociatedResource(
                    type = ResourceType.STUDY_GROUP_SCHEDULE,
                    resourceId = studySchedule.identifier
                )
            )
        )
    }

    override fun processGroupPush(): GroupPushMessage {
        return GroupPushMessage(
            groupId = studySchedule.groupId,
            title = group.name,
            body = "${studySchedule.session}회차 일정이 추가되었어요!",
            data = mapOf(
                "groupId" to group.identifier,
                "scheduleId" to studySchedule.identifier
            ),
        )
    }
}