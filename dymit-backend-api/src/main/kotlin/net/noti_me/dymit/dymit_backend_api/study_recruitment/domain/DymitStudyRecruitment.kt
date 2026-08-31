package net.noti_me.dymit.dymit_backend_api.study_recruitment.domain

import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import net.noti_me.dymit.dymit_backend_api.common.errors.TooManyRequestException
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
 * @property groupId 모집 대상 스터디 그룹 ObjectId
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
 * @property tags 모집글 태그 목록
 * @property bumpAt 마지막 끌어올리기 시각
 * @property bumpCount 끌어올리기 횟수
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 * @property isDeleted 삭제 여부
 */
@Document(collection = "study_recruitments")
@TypeAlias(DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS)
class DymitStudyRecruitment(
    id: ObjectId? = null,
    val writer: DymitStudyRecruitmentWriter,
    @Field("group_id")
    val groupId: ObjectId,
    val type: StudyRecruitmentType = StudyRecruitmentType.DYMIT,
    title: String,
    description: String,
    purpose: String,
    recruitmentStatus: DymitStudyRecruitmentStatus = DymitStudyRecruitmentStatus.RECRUITING,
    recruitmentStart: Instant? = null,
    recruitmentEnd: Instant? = null,
    targetMember: String,
    studyFormat: String,
    contact: Contact,
    tags: List<String> = emptyList(),
    bumpAt: Instant = Instant.now(),
    bumpCount: Int = 0,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<DymitStudyRecruitment>(
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
    var recruitmentStatus: DymitStudyRecruitmentStatus = recruitmentStatus
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

    var contact: Contact = contact
        private set

    var tags: List<String> = tags.toList()
        private set

    @Field("bumpAt")
    var bumpAt: Instant = bumpAt
        private set

    @Field("bumpCount")
    var bumpCount: Int = bumpCount
        private set

    /**
     * 모집글을 목록 최상단으로 끌어올립니다.
     *
     * @throws TooManyRequestException 최대 끌어올리기 횟수를 초과한 경우
     */
    fun bump() {
        if ( bumpCount >= MAX_BUMP_COUNT ) {
            throw TooManyRequestException(
                code = "EXCEED_BUMP_COUNT",
                message = "끌어올리기 최대 횟수를 초과하였습니다."
            )
        }

        bumpCount += 1
        bumpAt = Instant.now()
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
     * 모집글 제목을 변경합니다.
     *
     * @param newTitle 변경할 제목
     */
    fun changeTitle(newTitle: String) {
        title = validateLength(newTitle, TITLE_MAX_LENGTH, "제목")
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
     * @param newContact 변경할 연락처 정보
     */
    fun changeContact(newContact: Contact) {
        contact = newContact
        touchUpdatedAt()
    }

    /**
     * 모집 상태를 변경합니다.
     *
     * @param newStatus 변경할 모집 상태
     */
    fun changeRecruitmentStatus(newStatus: DymitStudyRecruitmentStatus) {
        recruitmentStatus = newStatus
        touchUpdatedAt()
    }

    /**
     * 모집글 태그 목록을 변경합니다.
     *
     * @param newTags 변경할 태그 목록
     */
    fun changeTags(newTags: List<String>) {
        tags = newTags.toList()
        touchUpdatedAt()
    }

    /**
     * 모집글을 삭제 상태로 변경합니다.
     */
    override fun markAsDeleted() {
        super.markAsDeleted()
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
        const val MAX_BUMP_COUNT = 5
        fun validateLength(value: String, maxLength: Int, fieldName: String): String {
            require(value.length <= maxLength) {
                "${fieldName}은(는) ${maxLength}자 이내로 작성해야 합니다."
            }
            return value
        }
    }
}

internal const val DYMIT_STUDY_RECRUITMENT_TYPE_ALIAS = "dymit_study_recruitment"
