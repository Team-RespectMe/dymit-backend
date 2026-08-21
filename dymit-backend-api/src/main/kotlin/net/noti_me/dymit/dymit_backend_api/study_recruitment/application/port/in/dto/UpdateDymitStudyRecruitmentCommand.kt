package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import java.time.Instant

/**
 * Dymit 스터디 모집글 수정 명령입니다.
 *
 * @property recruitmentId 모집글 식별자
 * @property title 모집글 제목
 * @property description 스터디 소개
 * @property purpose 스터디 목적
 * @property targetMember 모집 대상
 * @property studyFormat 운영 방식
 * @property contact 연락처 정보
 * @property recruitmentStart 모집 시작 시각
 * @property recruitmentEnd 모집 종료 시각
 * @property status 모집 상태
 * @property tags 태그 목록
 */
data class UpdateDymitStudyRecruitmentCommand(
    val recruitmentId: String,
    val title: String,
    val description: String,
    val purpose: String,
    val targetMember: String,
    val studyFormat: String,
    val contact: Contact,
    val recruitmentStart: Instant?,
    val recruitmentEnd: Instant?,
    val status: DymitStudyRecruitmentStatus,
    val tags: List<String> = emptyList()
)
