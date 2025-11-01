package net.noti_me.dymit.dymit_backend_api.domain.user_feed

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 그룹 피드 객체
 * - groupId: 피드가 속한 그룹 ID
 * - iconType: 피드 아이콘 타입
 *   eventName: 피드 이벤트 이름
 * - title: 피드 제목
 * - messages: 피드 메시지 목록
 * - associates: 피드에 첨부된 리소스 목록
 * - createdAt: 피드 생성 일시
 * - updatedAt: 피드 수정 일시
 * - isDeleted: 피드 삭제 여부
 */
@Document("group_feeds")
class GroupFeed(
    id: ObjectId? = null,
    @Indexed(name = "group_feed_group_id_idx")
    val groupId: ObjectId,
    val iconType: IconType,
    val eventName: String,
    val title: String = "Dymit",
    val messages: List<FeedMessage> = emptyList(),
    val associates: List<AssociatedResource> = emptyList(),
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
): BaseAggregateRoot<GroupFeed> (
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupFeed) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
