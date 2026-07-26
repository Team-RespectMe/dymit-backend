package net.noti_me.dymit.dymit_backend_api.task.application.port.`out`.file.dto

import org.bson.types.ObjectId

/**
 * 과제 모듈에서 사용하는 파일 상태입니다.
 */
enum class TaskFileStatusDto {
    REQUESTED,
    UPLOADED,
    LINKED,
    UNREFERENCED,
    DELETED_IN_S3,
    FAILED
}

/**
 * 과제 모듈에서 사용하는 파일 조회 DTO입니다.
 *
 * @param id 파일 식별자
 * @param originalFileName 원본 파일명
 * @param status 파일 상태
 * @param url 파일 접근 URL
 * @param thumbnailUrl 썸네일 접근 URL
 */
data class TaskFileDto(
    val id: ObjectId,
    val originalFileName: String,
    val status: TaskFileStatusDto,
    val url: String,
    val thumbnailUrl: String?
) {

    /**
     * 문자열 파일 식별자를 반환합니다.
     */
    val identifier: String
        get() = id.toHexString()
}
