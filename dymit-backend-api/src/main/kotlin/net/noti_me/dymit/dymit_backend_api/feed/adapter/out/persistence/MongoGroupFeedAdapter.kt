package net.noti_me.dymit.dymit_backend_api.feed.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.feed.application.port.out.persistence.GroupFeedPersistencePort
import net.noti_me.dymit.dymit_backend_api.feed.domain.GroupFeed
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository

/**
 * MongoDB를 사용하는 그룹 피드 출력 어댑터입니다.
 *
 * @param mongoTemplate MongoDB 접근 객체
 */
@Repository
class MongoGroupFeedAdapter(
    private val mongoTemplate: MongoTemplate
) : GroupFeedPersistencePort {

    /**
     * 그룹 피드를 저장합니다.
     *
     * @param groupFeed 저장할 그룹 피드
     * @return 저장된 그룹 피드
     */
    override fun save(groupFeed: GroupFeed): GroupFeed {
        return mongoTemplate.save(groupFeed)
    }

    /**
     * 식별자로 그룹 피드를 조회합니다.
     *
     * @param id 피드 식별자
     * @return 그룹 피드 또는 null
     */
    override fun findById(id: ObjectId): GroupFeed? {
        return mongoTemplate.findById(id, GroupFeed::class.java)
    }

    /**
     * 그룹 목록의 피드를 기존 커서 조건과 최신순 정렬로 조회합니다.
     *
     * @param groupIds 그룹 식별자 목록
     * @param cursor 조회 커서
     * @param size 조회 개수
     * @return 그룹 피드 목록
     */
    override fun findByGroupIdsOrderByIdDesc(
        groupIds: List<ObjectId>,
        cursor: ObjectId?,
        size: Long
    ): List<GroupFeed> {
        val query = Query()
            .addCriteria(Criteria.where("groupId").`in`(groupIds))
            .limit(size.toInt())
            .with(Sort.by(Sort.Direction.DESC, "_id"))

        cursor?.let { query.addCriteria(Criteria.where("_id").gt(it)) }
        return mongoTemplate.find(query, GroupFeed::class.java)
    }
}
