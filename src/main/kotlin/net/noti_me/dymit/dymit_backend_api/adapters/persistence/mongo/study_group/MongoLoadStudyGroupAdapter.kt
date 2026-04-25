package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Repository
import kotlin.jvm.java

@Repository
class MongoLoadStudyGroupAdapter(
    private val mongoTemplate: MongoTemplate
): LoadStudyGroupPort {

    override fun loadByGroupId(groupId: String): StudyGroup? {
        return mongoTemplate.findById(groupId, StudyGroup::class.java)
    }

    override fun loadByInviteCode(inviteCode: String): StudyGroup? {
        return mongoTemplate.findOne(
            Query(Criteria.where("inviteCode.code").`is`(inviteCode)),
            StudyGroup::class.java
        )
    }

    override fun loadByOwnerId(ownerId: String): List<StudyGroup> {
        return mongoTemplate.find(
           Query(Criteria.where("ownerId").`is`(ObjectId(ownerId))),
            StudyGroup::class.java,
        )
    }

    override fun existsByInviteCode(inviteCode: String): Boolean {
        return mongoTemplate.exists(
            Query(Criteria.where("inviteCode.code").`is`(inviteCode)),
            StudyGroup::class.java
        )
    }

    override fun loadByGroupIds(groupIds: List<String>): List<StudyGroup> {
        // convert string ids to ObjectId and sort by createdAt descending (most recent first)
        val objectIds = groupIds.map { ObjectId(it) }
        val query = Query(Criteria.where("_id").`in`(objectIds))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoTemplate.find(query, StudyGroup::class.java)
    }

    override fun countByOwnerId(ownerId: String): Long {
        return mongoTemplate.count(
            Query(Criteria.where("ownerId").`is`(ObjectId(ownerId))),
            StudyGroup::class.java
        )
    }
}