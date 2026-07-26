package net.noti_me.dymit.dymit_backend_api.study_schedule.application.port.out.file.dto

import org.bson.types.ObjectId

enum class StudyScheduleFileStatusDto {
    REQUESTED,
    UPLOADED,
    LINKED,
    UNREFERENCED,
    DELETED_IN_S3,
    FAILED
}

data class StudyScheduleFileDto(
    val id: ObjectId,
    val originalFileName: String,
    val path: String,
    val thumbnailPath: String?,
    val status: StudyScheduleFileStatusDto,
    val contentType: String?,
    val fileSize: Long,
    val url: String,
    val thumbnailUrl: String?
)
