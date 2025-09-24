package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group_member

import net.noti_me.dymit.dymit_backend_api.domain.study_group.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.domain.study_group.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository

@Repository
class MongoStudyGroupMemberRepository(
    private val mongoTemplate: MongoTemplate
): StudyGroupMemberRepository {

    override fun saveAll(members: List<StudyGroupMember>): List<StudyGroupMember> {
        val ops = mongoTemplate.bulkOps(
            BulkOperations.BulkMode.UNORDERED,
            StudyGroupMember::class.java
        )

        members.forEach { member ->
            val query = Query(Criteria.where("id").`is`(member.id))
            val update = Update()
            val doc = mongoTemplate.getConverter()
                .convertToMongoType(member) as Document
            doc.forEach { key, value ->
                if (key != "_id") {
                    update.set(key, value)
                }
            }
            ops.upsert(query, update)
        }
        ops.execute()
        return members
    }

    override fun persist(member: StudyGroupMember): StudyGroupMember {
        return mongoTemplate.save(member)
    }

    override fun update(member: StudyGroupMember): StudyGroupMember {
        return mongoTemplate.save(member)
    }

    override fun delete(member: StudyGroupMember): Boolean {
        val query = Query(Criteria.where("_id").`is`(member.id))
        return mongoTemplate.remove(query, StudyGroupMember::class.java).deletedCount > 0
    }

    override fun findByMemberId(
        memberId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMember> {
        val criteria = Criteria.where("memberId").`is`(memberId)
        if (cursor != null) {
            criteria.and("_id").lte(cursor)
        }
        val query = Query(criteria)
            .with(Sort.by(Sort.Direction.DESC, "_id"))
            .limit(limit)
        return mongoTemplate.find(query, StudyGroupMember::class.java)
    }

    override fun findByGroupIdAndMemberId(
        groupId: ObjectId,
        memberId: ObjectId
    ): StudyGroupMember? {
        val query = Query(Criteria.where("groupId").`is`(groupId).and("memberId").`is`(memberId))
        return mongoTemplate.findOne(query, StudyGroupMember::class.java)
    }

    override fun findByGroupId(groupId: ObjectId): List<StudyGroupMember> {
        val query = Query(Criteria.where("groupId").`is`(groupId))
        // sort by createdAt
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoTemplate.find(query, StudyGroupMember::class.java)
    }

    override fun countByGroupId(groupId: ObjectId): Long {
        val query = Query(Criteria.where("groupId").`is`(groupId))
        return mongoTemplate.count(query, StudyGroupMember::class.java)
    }

    override fun findByGroupIdsOrderByCreatedAt(
        groupIds: List<ObjectId>,
        limit: Int
    ): Map<String, List<StudyGroupMember>> {
        val query = Query(Criteria.where("groupId").`in`(groupIds))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(limit)

        val members = mongoTemplate.find(query, StudyGroupMember::class.java)

        return members.groupBy { it.groupId.toHexString() }
            .mapValues { it.value.sortedByDescending { member -> member.createdAt } }
    }

    override fun findGroupIdsByMemberId(memberId: ObjectId): List<String> {
        val query = Query(Criteria.where("memberId").`is`(memberId))
        return mongoTemplate.find(query, StudyGroupMember::class.java)
            .map { it.groupId.toHexString() }
            .distinct()
    }

    override fun findByGroupIdAndMemberIdsIn(
        groupId: ObjectId,
        memberIds: List<ObjectId>
    ): List<StudyGroupMember> {
        return mongoTemplate.find(
            Query(Criteria.where("groupId").`is`(groupId).and("memberId").`in`(memberIds)),
            StudyGroupMember::class.java
        )
    }

    override fun countByMemberIdAndRole(memberId: ObjectId, role: GroupMemberRole): Long {
        return mongoTemplate.count(
            Query(Criteria.where("memberId").`is`(memberId).and("role").`is`(role)),
            StudyGroupMember::class.java
        )
    }
}