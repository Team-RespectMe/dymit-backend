package net.noti_me.dymit.dymit_backend_api.study_schedule.domain

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.study_schedule.domain.ScheduleCommentWriter
import net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.study_group.dto.StudyScheduleGroupMemberDto as StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "schedule_comments")
@TypeAlias("net.noti_me.dymit.dymit_backend_api.domain.study_schedule.ScheduleComment")
class ScheduleComment(
//    @Id
//    val id: ObjectId = ObjectId.get(),
    id: ObjectId? = null,
    @Indexed
    val scheduleId: ObjectId,
    val writer: ScheduleCommentWriter,
    content: String,
    createdAt: Instant? = null,
    updatedAt: Instant? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<ScheduleComment>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

//    val identifier: String
//        get() = id.toHexString()

    var content: String = content
        private set

    /**
     * 댓글 내용을 수정합니다.
     * @param requester 요청자 정보
     * @param newContent 새로운 댓글 내용
     * @throws ForbiddenException 댓글 작성자가 아닌 경우
     */
    fun updateContent(requester: StudyGroupMember, newContent: String) {
        if (requester.memberId != writer.id) {
            throw ForbiddenException(message = "댓글 작성자만 댓글을 수정할 수 있습니다.")
        }

        if ( newContent.length > 500 ) {
            throw IllegalArgumentException("댓글 내용은 500자 이내로 작성해야 합니다.")
        }
        this.content = newContent
    }
}
