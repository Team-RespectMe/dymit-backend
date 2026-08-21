package net.noti_me.dymit.dymit_backend_api.study_recruitment.application.port.out.persistence.dto

import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.Contact
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitment
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentStatus
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.DymitStudyRecruitmentWriter
import net.noti_me.dymit.dymit_backend_api.study_recruitment.domain.StudyRecruitmentType
import org.bson.types.ObjectId
import java.time.Instant
import java.time.LocalDateTime

/**
 * Dymit 스터디 모집글 영속성 DTO입니다.
 *
 * @property id 모집글 ObjectId
 * @property writerId 작성자 ObjectId
 * @property writerNickname 작성자 닉네임
 * @property groupId 그룹 ObjectId
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
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 * @property isDeleted 삭제 여부
 */
data class DymitStudyRecruitmentPersistenceDto(
    val id: ObjectId,
    val writerId: ObjectId,
    val writerNickname: String,
    val groupId: ObjectId,
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
    val isDeleted: Boolean
) {

    /**
     * 영속성 DTO를 도메인 엔티티로 변환합니다.
     *
     * @return Dymit 스터디 모집글 엔티티
     */
    fun toDomain(): DymitStudyRecruitment {
        return DymitStudyRecruitment(
            id = id,
            writer = DymitStudyRecruitmentWriter(
                id = writerId,
                nickname = writerNickname
            ),
            groupId = groupId,
            type = type,
            title = title,
            description = description,
            purpose = purpose,
            recruitmentStatus = recruitmentStatus,
            recruitmentStart = recruitmentStart,
            recruitmentEnd = recruitmentEnd,
            targetMember = targetMember,
            studyFormat = studyFormat,
            contact = contact,
            tags = tags,
            createdAt = createdAt,
            updatedAt = updatedAt,
            isDeleted = isDeleted
        )
    }

    companion object {

        /**
         * 도메인 엔티티를 영속성 DTO로 변환합니다.
         *
         * @param recruitment Dymit 스터디 모집글 엔티티
         * @return 영속성 DTO
         */
        fun from(recruitment: DymitStudyRecruitment): DymitStudyRecruitmentPersistenceDto {
            return DymitStudyRecruitmentPersistenceDto(
                id = requireNotNull(recruitment.id) {
                    "저장된 Dymit 스터디 모집글에는 id가 필요합니다."
                },
                writerId = recruitment.writer.id,
                writerNickname = recruitment.writer.nickname,
                groupId = recruitment.groupId,
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
                isDeleted = recruitment.isDeleted
            )
        }
    }
}
