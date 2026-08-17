package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.dto

import org.bson.types.ObjectId

/**
 * Dymit 스터디 모집글이 사용하는 회원 조회 DTO입니다.
 *
 * @property id 회원 식별자
 * @property profileImageUrl 최신 프로필 이미지 썸네일 URL
 */
data class DymitStudyRecruitmentMemberDto(
    val id: ObjectId,
    val profileImageUrl: String
)
