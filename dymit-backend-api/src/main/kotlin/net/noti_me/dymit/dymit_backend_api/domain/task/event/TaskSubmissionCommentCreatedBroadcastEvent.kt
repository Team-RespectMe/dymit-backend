package net.noti_me.dymit.dymit_backend_api.domain.task.event

import net.noti_me.dymit.dymit_backend_api.common.event.BroadcastEvent
import net.noti_me.dymit.dymit_backend_api.push_notification.domain.PersonalPushMessage
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupDto as StudyGroup
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.`in`.server_to_server.dto.StudyGroupMemberDto as StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.domain.task.Task
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.AssociatedResource
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.FeedMessage
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.IconType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.ResourceType
import net.noti_me.dymit.dymit_backend_api.domain.user_feed.UserFeed
import org.bson.types.ObjectId

/**
 * 과제 제출 댓글 생성 브로드캐스트 이벤트입니다.
 *
 * @param group 과제가 속한 스터디 그룹
 * @param task 댓글 대상 과제
 * @param member 댓글 작성 멤버
 * @param assigneeMemberId 댓글 대상 제출의 작성자 멤버 ID
 */
class TaskSubmissionCommentCreatedBroadcastEvent(
    private val group: StudyGroup,
    private val task: Task,
    private val member: StudyGroupMember,
    private val assigneeMemberId: ObjectId
) : BroadcastEvent(listOf(assigneeMemberId)) {

    private val eventName = "TASK_SUBMISSION_COMMENT_CREATED"
    private val message = "${member.nickname} 님이 회원님의 과제 제출에 댓글을 달았습니다."

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
                    "assigneeId" to assigneeMemberId.toHexString()
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
                iconType = IconType.NOTICE,
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
                        resourceId = assigneeMemberId.toHexString()
                    )
                )
            )
        }
    }
}
