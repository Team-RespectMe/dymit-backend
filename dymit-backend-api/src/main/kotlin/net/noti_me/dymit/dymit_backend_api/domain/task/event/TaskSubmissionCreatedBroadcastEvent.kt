package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastEvent
import net.noti_me.dymit.dymit_backend_api.domain.push.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
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
) : BroadcastEvent(memberIds.distinct()) {

    private val eventName = "TASK_SUBMISSION_CREATED"
    private val message = "${group.name}의 과제 ${task.title}를 ${member.nickname} 님이 제출했어요."

    /**
     * 수신 대상별 개인 푸시 메시지를 생성합니다.
     *
     * @return 개인 푸시 메시지 목록
     */
    override fun processPushMessages(): List<PersonalPushMessage> {
        return memberIds.map { memberId ->
            PersonalPushMessage(
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
    override fun processUserFeeds(): List<UserFeed> {
        return memberIds.map { memberId ->
            UserFeed(
                memberId = memberId,
                iconType = IconType.CHECK,
                eventName = eventName,
                messages = listOf(FeedMessage(text = message)),
                associates = listOf(
                    AssociatedResource(
                        type = ResourceType.STUDY_GROUP,
                        resourceId = group.identifier
                    ),
                    AssociatedResource(
                        type = ResourceType.TASK,
                        resourceId = task.identifier
                    ),
                    AssociatedResource(
                        type = ResourceType.MEMBER,
                        resourceId = member.memberId.toHexString()
                    )
                )
            )
        }
    }
}
