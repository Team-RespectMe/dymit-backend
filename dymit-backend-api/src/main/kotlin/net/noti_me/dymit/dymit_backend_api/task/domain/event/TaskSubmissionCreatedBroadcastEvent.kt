package net.noti_me.dymit.dymit_backend_api.task.domain.event

import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEvent
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushEventData
import net.noti_me.dymit.dymit_backend_api.common.event.push.PersonalPushMessagesEvent
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventIconType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventMessage
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResource
import net.noti_me.dymit.dymit_backend_api.common.event.feed.FeedEventResourceType
import net.noti_me.dymit.dymit_backend_api.common.event.feed.PersonalFeedEventData
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.task.domain.Task
import org.bson.types.ObjectId

/**
 * 과제 제출 생성 브로드캐스트 이벤트입니다.
 *
 * @param group 과제가 속한 스터디 그룹
 * @param task 제출 대상 과제
 * @param member 제출한 멤버
 * @param memberIds 수신 대상 멤버 ID 목록
 */
class TaskSubmissionCreatedBroadcastEvent(
    private val group: StudyGroup,
    private val task: Task,
    private val member: StudyGroupMember,
    memberIds: List<ObjectId>
) : PersonalPushMessagesEvent, PersonalFeedEvent {

    private val distinctMemberIds = memberIds.distinct()
    private val eventName = "TASK_SUBMISSION_CREATED"
    private val message = "${group.name}의 과제 ${task.title}를 ${member.nickname} 님이 제출했어요."

    /**
     * 수신 대상별 개인 푸시 메시지를 생성합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    override fun toPersonalPushMessages(): List<PersonalPushEventData> {
        return distinctMemberIds.map { memberId ->
            PersonalPushEventData(
                memberId = memberId,
                eventName = eventName,
                title = group.name,
                body = message,
                image = null,
                data = mapOf(
                    "groupId" to group.identifier,
                    "taskId" to task.identifier,
                    "assigneeId" to member.memberId.toHexString()
                )
            )
        }
    }

    /**
     * 수신 대상별 사용자 피드를 생성합니다.
     *
     * @return 사용자 피드 목록
     */
    override fun toPersonalFeedData(): List<PersonalFeedEventData> {
        return distinctMemberIds.map { memberId ->
            PersonalFeedEventData(
                memberId = memberId.toHexString(),
                iconType = FeedEventIconType.CHECK,
                eventName = eventName,
                messages = listOf(FeedEventMessage(text = message)),
                resources = listOf(
                    FeedEventResource(
                        type = FeedEventResourceType.STUDY_GROUP,
                        resourceId = group.identifier
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.TASK,
                        resourceId = task.identifier
                    ),
                    FeedEventResource(
                        type = FeedEventResourceType.MEMBER,
                        resourceId = member.memberId.toHexString()
                    )
                )
            )
        }
    }
}
