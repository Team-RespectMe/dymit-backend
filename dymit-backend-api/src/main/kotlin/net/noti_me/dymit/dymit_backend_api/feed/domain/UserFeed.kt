package net.noti_me.dymit.dymit_backend_api.feed.domain

import net.noti_me.dymit.dymit_backend_api.domain.BaseAggregateRoot
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 한 회원에게 노출되는 개인 피드입니다.
 *
 * @param id 피드 식별자
 * @param memberId 대상 회원 식별자
 * @param iconType 아이콘 종류
 * @param eventName 이벤트 이름
 * @param messages 표시할 메시지 목록
 * @param associates 연결 리소스 목록
 * @param isRead 읽음 여부
 * @param createdAt 생성 시각
 * @param updatedAt 수정 시각
 * @param isDeleted 삭제 여부
 */
@Document(collection = "user_feeds")
@CompoundIndex(name = "user_feeds_memberId_createdAt_idx", def = "{'memberId': 1, 'createdAt': -1}")
class UserFeed(
    id: ObjectId? = null,
    val memberId: ObjectId,
    val iconType: IconType,
    val eventName: String,
    val messages: List<FeedMessage>,
    val associates: List<AssociatedResource>,
    isRead: Boolean = false,
    createdAt: LocalDateTime? = null,
    updatedAt: LocalDateTime? = null,
    isDeleted: Boolean = false
) : BaseAggregateRoot<UserFeed>(
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isDeleted = isDeleted
) {

    companion object {

        /**
         * 그룹 피드를 개인 피드로 변환합니다.
         *
         * @param memberId 대상 회원 식별자
         * @param groupFeed 원본 그룹 피드
         * @return 생성한 개인 피드
         */
        fun create(memberId: ObjectId, groupFeed: GroupFeed): UserFeed {
            assert(!groupFeed.isDeleted) { "삭제된 그룹 피드로부터 피드를 생성할 수 없습니다." }
            return UserFeed(
                memberId = memberId,
                iconType = groupFeed.iconType,
                messages = groupFeed.messages,
                associates = groupFeed.associates,
                eventName = groupFeed.eventName,
                createdAt = groupFeed.createdAt,
                updatedAt = groupFeed.updatedAt
            )
        }
    }

    var isRead: Boolean = isRead
        private set

    /**
     * 피드를 읽음 상태로 변경합니다.
     */
    fun markAsRead() {
        if (!isRead) {
            isRead = true
        }
    }

    /**
     * 주어진 회원이 피드 소유자인지 확인합니다.
     *
     * @param memberId 회원 식별자
     * @return 소유 여부
     */
    fun isOwnedBy(memberId: String): Boolean {
        return this.memberId.toHexString() == memberId
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserFeed) return false
        if (id == null || other.id == null) return false
        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
