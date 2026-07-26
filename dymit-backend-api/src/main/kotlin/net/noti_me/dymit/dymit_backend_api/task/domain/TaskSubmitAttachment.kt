package net.noti_me.dymit.dymit_backend_api.task.domain

import net.noti_me.dymit.dymit_backend_api.common.errors.BadRequestException
import org.bson.types.ObjectId

/**
 * 제출 본문 첨부 값 객체입니다.
 *
 * @param type 첨부 타입(URL/FILE)
 * @param title 첨부 표시 이름
 * @param url URL 첨부 주소
 * @param fileId 파일 첨부 ID
 */
class TaskSubmitAttachment(
    val type: TaskSubmitAttachmentType,
    val title: String,
    val url: String? = null,
    val fileId: ObjectId? = null
) {

    init {
        if ( title.isBlank() ) {
            throw BadRequestException(message = "첨부 제목은 비어 있을 수 없습니다.")
        }

        if ( title.length > 255 ) {
            throw BadRequestException(message = "첨부 제목은 255자를 초과할 수 없습니다.")
        }

        if ( type == TaskSubmitAttachmentType.URL && url.isNullOrBlank() ) {
            throw BadRequestException(message = "URL 첨부는 주소가 필요합니다.")
        }

        if ( type == TaskSubmitAttachmentType.FILE && fileId == null ) {
            throw BadRequestException(message = "파일 첨부는 파일 ID가 필요합니다.")
        }
    }
}
