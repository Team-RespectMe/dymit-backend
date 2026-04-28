package net.noti_me.dymit.dymit_backend_api.domain.study_recruitment

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 스터디 모집글 도메인 엔티티입니다.
 *
 * @property id MongoDB ObjectId
 * @property externalId 외부 원본 시스템의 모집글 식별자
 * @property type 모집글 출처 타입
 * @property title 모집글 제목
 * @property content 모집글 본문
 * @property url 원본 모집글 URL
 * @property writer 원본 작성자명
 * @property createdAt 생성 시각
 * @property updatedAt 수정 시각
 * @property isDeleted 삭제 여부
 */
@Document(collection = "study_recruitments")
class StudyRecruitment(
    id: ObjectId? = null,
    externalId: String,
    type: String,
    title: String,
    content: String,
    url: String,
    writer: String,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<StudyRecruitment>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    var externalId: String = externalId
        private set

    var type: String = type
        private set

    var title: String = title
        private set

    var content: String = content
        private set

    var url: String = url
        private set

    var writer: String = writer
        private set

    /**
     * 모집글 핵심 내용을 수정합니다.
     *
     * @param title 수정할 제목
     * @param content 수정할 본문
     * @param url 수정할 원본 URL
     * @param writer 수정할 작성자명
     * @return Unit
     */
    fun updateCore(
        title: String,
        content: String,
        url: String,
        writer: String
    ) {
        this.title = title
        this.content = content
        this.url = url
        this.writer = writer
        this.updatedAt = LocalDateTime.now()
    }

    override fun equals(other: Any?): Boolean {
        if ( this === other ) return true
        if ( other !is StudyRecruitment ) return false
        if ( id == null || other.id == null ) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
