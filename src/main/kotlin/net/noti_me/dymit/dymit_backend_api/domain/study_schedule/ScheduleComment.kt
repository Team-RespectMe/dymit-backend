package net.noti_me.dymit.dymit_backend_api.domain.study_schedule

import net.noti_me.dymit.dymit_backend_api.common.errors.ForbiddenException
import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.domain.board.Writer
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = "schedule_comments")
class ScheduleComment(
    @Id
    val id: ObjectId = ObjectId.get(),
    @Indexed
    val scheduleId: ObjectId,
    val writer: Writer,
    content: String,
) : BaseAggregateRoot<ScheduleComment>() {

    val identifier: String
        get() = id.toHexString()

    var content: String = content
        private set

    /**
     * 댓글 내용을 수정합니다.
     * @param requester 요청자 정보
     * @param newContent 새로운 댓글 내용
     * @throws ForbiddenException 댓글 작성자가 아닌 경우
     */
    fun updateContent(requester: StudyGroupMember, newContent: String) {
        if (requester.id != writer.id) {
            throw ForbiddenException(message = "댓글 작성자만 댓글을 수정할 수 있습니다.")
        }

        if ( newContent.length > 500 ) {
            throw IllegalArgumentException("댓글 내용은 500자 이내로 작성해야 합니다.")
        }
        this.content = newContent
    }
}
