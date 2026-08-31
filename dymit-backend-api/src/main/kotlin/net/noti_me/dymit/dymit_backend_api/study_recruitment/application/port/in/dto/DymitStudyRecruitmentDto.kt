package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.`in`.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.member.dto.DymitStudyRecruitmentMemberDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto.DymitStudyRecruitmentPersistenceDto
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import java.time.Instant
import java.time.LocalDateTime

/**
 * Dymit 스터디 모집글 입력 포트 DTO입니다.
 *
 * @property id 모집글 식별자
 * @property writerId 작성자 식별자
 * @property writerNickname 작성자 닉네임
 * @property writerProfileImageUrl 작성자 최신 프로필 이미지 썸네일 URL
 * @property groupId 그룹 식별자
 * @property type 모집글 출처 유형
 * @property title 모집글 제목
 * @property description 스터디 소개
 * @property purpose 스터디 목적
 * @property recruitmentStatus 모집 상태
 * @property recruitmentStart 모집 시작 시각
 * @property recruitmentEnd 모집 종료 시각
 * @property targetMember 모집 대상
 * @property studyFormat 운영 방식
 * @property contact 연락처 정보
 * @property tags 태그 목록
 * @property bumpAt 마지막 끌어올리기 시각
 * @property bumpCount 끌어올리기 횟수
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 */
data class DymitStudyRecruitmentDto(
    val id: String,
    val writerId: String,
    val writerNickname: String,
    val writerProfileImageUrl: String,
    val groupId: String,
    val type: StudyRecruitmentType,
    val title: String,
    val description: String,
    val purpose: String,
    val recruitmentStatus: DymitStudyRecruitmentStatus,
    val recruitmentStart: Instant?,
    val recruitmentEnd: Instant?,
    val targetMember: String,
    val studyFormat: String,
    val contact: Contact,
    val tags: List<String>,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
    val bumpAt: Instant = Instant.EPOCH,
    val bumpCount: Int = 0
) {

    companion object {

        /**
         * 영속성 DTO를 입력 포트 DTO로 변환합니다.
         *
         * @param recruitment 영속성 계층 모집글 DTO
         * @param writer 최신 프로필 이미지를 포함한 작성자 DTO
         * @return 입력 포트용 모집글 DTO
         */
        fun from(
            recruitment: DymitStudyRecruitmentPersistenceDto,
            writer: DymitStudyRecruitmentMemberDto
        ): DymitStudyRecruitmentDto {
            return DymitStudyRecruitmentDto(
                id = recruitment.id.toHexString(),
                writerId = recruitment.writerId.toHexString(),
                writerNickname = recruitment.writerNickname,
                writerProfileImageUrl = writer.profileImageUrl,
                groupId = recruitment.groupId.toHexString(),
                type = recruitment.type,
                title = recruitment.title,
                description = recruitment.description,
                purpose = recruitment.purpose,
                recruitmentStatus = recruitment.recruitmentStatus,
                recruitmentStart = recruitment.recruitmentStart,
                recruitmentEnd = recruitment.recruitmentEnd,
                targetMember = recruitment.targetMember,
                studyFormat = recruitment.studyFormat,
                contact = recruitment.contact,
                tags = recruitment.tags,
                createdAt = recruitment.createdAt,
                updatedAt = recruitment.updatedAt,
                bumpAt = recruitment.bumpAt,
                bumpCount = recruitment.bumpCount
            )
        }
    }
}
