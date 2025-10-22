package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group

import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroup
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group.LoadStudyGroupPort
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
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
        return mongoTemplate.find(
            Query(Criteria.where("_id").`in`(groupIds)),
            StudyGroup::class.java
        )
    }

    override fun countByOwnerId(ownerId: String): Long {
        return mongoTemplate.count(
            Query(Criteria.where("ownerId").`is`(ObjectId(ownerId))),
            StudyGroup::class.java
        )
    }
}