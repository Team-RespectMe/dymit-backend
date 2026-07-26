package net.noti_me.dymit.dymit_backend_api.feed.domain

import net.noti_me.dymit.dymit_backend_api.common.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 그룹 구성원에게 전파할 피드입니다.
 *
 * @param id 피드 식별자
 * @param groupId 대상 그룹 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param title 피드 제목
 * @param messages 표시할 메시지 목록
 * @param associates 연결 리소스 목록
 * @param excludedMemberIds 피드 생성 제외 회원 식별자
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param isDeleted 삭제 여부
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
    val excludedMemberIds: MutableSet<ObjectId> = mutableSetOf(),
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<GroupFeed>(
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
