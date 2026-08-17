package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.study_group.dto

import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글이 사용하는 그룹 정보 DTO입니다.
 *
 * @property id 그룹 ObjectId
 * @property ownerId 그룹 소유자 ObjectId
 * @property name 그룹 이름
 */
data class DymitStudyRecruitmentStudyGroupDto(
    val id: ObjectId,
    val ownerId: ObjectId,
    val name: String
)
