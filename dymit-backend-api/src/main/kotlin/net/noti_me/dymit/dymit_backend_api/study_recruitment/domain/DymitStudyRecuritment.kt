package net.noti_me.dymit.dymit_backend_api.study_recruitment.domain

import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.Instant
import java.time.LocalDateTime

/**
 * Dymit 내부 스터디 모집글 도메인 엔티티입니다.
 *
 * @property id MongoDB ObjectId
 * @property writer 모집글 작성자
 * @property group 모집 대상 스터디 그룹
 * @property title 모집글 제목
 * @property description 스터디 소개
 * @property purpose 스터디 목적
 * @property recruitmentStatus 모집 상태
 * @property recruitmentStart 모집 시작 시각
 * @property recruitmentEnd 모집 종료 시각
 * @property targetMember 모집 대상
 * @property studyFormat 운영 방식
 * @property contact 연락처 또는 연락 URL
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 * @property isDeleted 삭제 여부
 */
@Document(collection = "study_recruitments")
@TypeAlias(DYMIT_STUDY_RECURITMENT_TYPE_ALIAS)
class DymitStudyRecuritment(
    id: ObjectId? = null,
    val writer: DymitStudyRecuritmentWriter,
    val group: DymitStudyRecuritmentGroup,
    title: String,
    description: String,
    purpose: String,
    recruitmentStatus: DymitStudyRecuritmentStatus = DymitStudyRecuritmentStatus.RECRUITING,
    recruitmentStart: Instant? = null,
    recruitmentEnd: Instant? = null,
    targetMember: String,
    studyFormat: String,
    contact: String,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<DymitStudyRecuritment>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var title: String = validateLength(title, TITLE_MAX_LENGTH, "제목")
        private set

    var description: String = validateLength(description, DESCRIPTION_MAX_LENGTH, "소개")
        private set

    var purpose: String = validateLength(purpose, PURPOSE_MAX_LENGTH, "목적")
        private set

    @Field("recruitment_status")
    var recruitmentStatus: DymitStudyRecuritmentStatus = recruitmentStatus
        private set

    @Field("recruitment_start")
    var recruitmentStart: Instant? = recruitmentStart
        private set

    @Field("recruitment_end")
    var recruitmentEnd: Instant? = recruitmentEnd
        private set

    @Field("target_member")
    var targetMember: String = validateLength(targetMember, TARGET_MEMBER_MAX_LENGTH, "모집 대상")
        private set

    @Field("study_format")
    var studyFormat: String = validateLength(studyFormat, STUDY_FORMAT_MAX_LENGTH, "운영 방식")
        private set

    var contact: String = validateLength(contact, CONTACT_MAX_LENGTH, "연락처")
        private set

    /**
     * 모집글 제목을 변경합니다.
     *
     * @param newTitle 변경할 제목
     */
    fun changeTitle(newTitle: String) {
        title = validateLength(newTitle, TITLE_MAX_LENGTH, "제목")
        touchUpdatedAt()
    }

    /**
     * 스터디 소개를 변경합니다.
     *
     * @param newDescription 변경할 소개
     */
    fun changeDescription(newDescription: String) {
        description = validateLength(newDescription, DESCRIPTION_MAX_LENGTH, "소개")
        touchUpdatedAt()
    }

    /**
     * 스터디 목적을 변경합니다.
     *
     * @param newPurpose 변경할 목적
     */
    fun changePurpose(newPurpose: String) {
        purpose = validateLength(newPurpose, PURPOSE_MAX_LENGTH, "목적")
        touchUpdatedAt()
    }

    /**
     * 모집 시작 시각을 변경합니다.
     *
     * @param newRecruitmentStart 변경할 모집 시작 시각
     */
    fun changeRecruitmentStart(newRecruitmentStart: Instant?) {
        recruitmentStart = newRecruitmentStart
        touchUpdatedAt()
    }

    /**
     * 모집 종료 시각을 변경합니다.
     *
     * @param newRecruitmentEnd 변경할 모집 종료 시각
     */
    fun changeRecruitmentEnd(newRecruitmentEnd: Instant?) {
        recruitmentEnd = newRecruitmentEnd
        touchUpdatedAt()
    }

    /**
     * 모집 대상을 변경합니다.
     *
     * @param newTargetMember 변경할 모집 대상
     */
    fun changeTargetMember(newTargetMember: String) {
        targetMember = validateLength(newTargetMember, TARGET_MEMBER_MAX_LENGTH, "모집 대상")
        touchUpdatedAt()
    }

    /**
     * 운영 방식을 변경합니다.
     *
     * @param newStudyFormat 변경할 운영 방식
     */
    fun changeStudyFormat(newStudyFormat: String) {
        studyFormat = validateLength(newStudyFormat, STUDY_FORMAT_MAX_LENGTH, "운영 방식")
        touchUpdatedAt()
    }

    /**
     * 연락처를 변경합니다.
     *
     * @param newContact 변경할 연락처 또는 연락 URL
     */
    fun changeContact(newContact: String) {
        contact = validateLength(newContact, CONTACT_MAX_LENGTH, "연락처")
        touchUpdatedAt()
    }

    private fun touchUpdatedAt() {
        updatedAt = LocalDateTime.now()
    }

    private companion object {

        const val TITLE_MAX_LENGTH = 50
        const val DESCRIPTION_MAX_LENGTH = 200
        const val PURPOSE_MAX_LENGTH = 50
        const val TARGET_MEMBER_MAX_LENGTH = 100
        const val STUDY_FORMAT_MAX_LENGTH = 100
        const val CONTACT_MAX_LENGTH = 255

        fun validateLength(value: String, maxLength: Int, fieldName: String): String {
            require(value.length <= maxLength) {
                "${fieldName}은(는) ${maxLength}자 이내로 작성해야 합니다."
            }
            return value
        }
    }
}

/**
 * Dymit 내부 스터디 모집글 작성자 값 객체입니다.
 *
 * @property id 작성자 ObjectId
 * @property nickname 작성자 표시 닉네임
 */
data class DymitStudyRecuritmentWriter(
    val id: ObjectId,
    val nickname: String
)

/**
 * Dymit 내부 스터디 모집글 그룹 값 객체입니다.
 *
 * @property id 그룹 ObjectId
 * @property name 그룹 표시 이름
 */
data class DymitStudyRecuritmentGroup(
    val id: ObjectId,
    val name: String
)

/**
 * Dymit 내부 스터디 모집 상태입니다.
 */
enum class DymitStudyRecuritmentStatus {
    RECRUITING,
    DONE
}

internal const val DYMIT_STUDY_RECURITMENT_TYPE_ALIAS = "dymit_study_recuritment"
