package net.noti_me.dymit.dymit_backend_api.domain.study_schedule

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 스터디 일정과 파일의 첨부 관계를 저장하는 도메인 엔티티입니다.
 *
 * @param scheduleId 첨부가 연결된 스터디 일정 ID
 * @param fileId 연결된 파일 ID
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param isDeleted 삭제 여부
 * @param id 문서 ID
 */
@Document(collection = "schedule_attachments")
class ScheduleAttachment(
    @Indexed(name = "schedule_attachment_schedule_id_idx")
    val scheduleId: ObjectId,
    @Indexed(name = "schedule_attachment_file_id_idx")
    val fileId: ObjectId,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false,
    id: ObjectId? = null
) : BaseAggregateRoot<ScheduleAttachment>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
)
