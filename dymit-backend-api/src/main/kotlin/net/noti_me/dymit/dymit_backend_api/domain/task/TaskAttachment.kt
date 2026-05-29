package net.noti_me.dymit.dymit_backend_api.domain.task

import org.bson.types.ObjectId

/**
 * 과제 본문에 연결되는 파일 첨부 값 객체입니다.
 *
 * @param fileId 첨부 파일 ID
 */
class TaskAttachment(
    val fileId: ObjectId
)
