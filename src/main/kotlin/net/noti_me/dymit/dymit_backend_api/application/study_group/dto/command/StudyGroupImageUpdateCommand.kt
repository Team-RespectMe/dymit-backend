package net.noti_me.dymit.dymit_backend_api.application.study_group.dto.command

import net.noti_me.dymit.dymit_backend_api.domain.ProfileImageType
import org.springframework.web.multipart.MultipartFile

/**
 * 스터디 그룹 이미지 업데이트 커맨드
 * @param groupId 스터디 그룹 ID
 * @param type 업데이트할 이미지 타입 (e.g., "preset", "external")
 * @param value preset 인 경우 프리셋 번호, external이라면 emptyString
 * @param file 업로드할 이미지 파일 (MultipartFile)
 */
class StudyGroupImageUpdateCommand(
    val groupId: String,
    val type: ProfileImageType,
    val value: Int?,
    val file: MultipartFile?
) {
}