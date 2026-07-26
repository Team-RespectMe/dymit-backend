package net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.domain.GroupFeed
import org.bson.types.ObjectId

/**
 * 그룹 피드 영속성 경계입니다.
 */
interface GroupFeedPersistencePort {

    /**
     * 그룹 피드를 저장합니다.
     *
     * @param groupFeed 저장할 그룹 피드
     * @return 저장된 그룹 피드
     */
    fun save(groupFeed: GroupFeed): GroupFeed

    /**
     * 식별자로 그룹 피드를 조회합니다.
     *
     * @param id 피드 식별자
     * @return 조회된 피드 또는 null
     */
    fun findById(id: ObjectId): GroupFeed?

    /**
     * 그룹 목록의 피드를 커서 이후부터 최신순으로 조회합니다.
     *
     * @param groupIds 그룹 식별자 목록
     * @param cursor 조회 커서
     * @param size 조회 개수
     * @return 그룹 피드 목록
     */
    fun findByGroupIdsOrderByIdDesc(
        groupIds: List<ObjectId>,
        cursor: ObjectId?,
        size: Long
    ): List<GroupFeed>
}
