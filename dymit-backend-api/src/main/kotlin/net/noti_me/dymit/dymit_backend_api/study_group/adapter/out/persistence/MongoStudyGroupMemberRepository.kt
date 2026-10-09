package net.noti_me.dymit.dymit_backend_api.study_group.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.study_group.domain.GroupMemberRole
import net.noti_me.dymit.dymit_backend_api.study_group.domain.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.study_group.application.port.out.persistence.StudyGroupMemberRepository
import org.bson.Document
import org.bson.types.ObjectId
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Repository
import java.time.Instant

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
            val query = Query(Criteria.where("_id").`is`(member.id))
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
        val id = member.id ?: return false
        val now = Instant.now()
        val result = mongoTemplate.updateFirst(
            Query(
                Criteria.where("_id").`is`(id)
                    .and("isDeleted").ne(true)
            ),
            Update()
                .set("isDeleted", true)
                .set("deletedAt", now)
                .set("updatedAt", now),
            StudyGroupMember::class.java
        )
        return result.modifiedCount > 0
    }

    override fun findByMemberId(
        memberId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMember> {
        val criteria = Criteria.where("memberId").`is`(memberId).and("isDeleted").ne(true)
        if (cursor != null) {
            criteria.and("_id").lt(cursor)
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
        val query = Query(Criteria.where("groupId").`is`(groupId)
            .and("memberId").`is`(memberId)
            .and("isDeleted").ne(true))
        return mongoTemplate.findOne(query, StudyGroupMember::class.java)
    }

    override fun findByIdIncludingDeleted(membershipId: ObjectId): StudyGroupMember? {
        return mongoTemplate.findById(membershipId, StudyGroupMember::class.java)
    }

    override fun findByGroupId(groupId: ObjectId): List<StudyGroupMember> {
        val query = Query(
            Criteria.where("groupId").`is`(groupId)
                .and("isDeleted").`is`(false)
        )
        // sort by createdAt
        query.with(Sort.by(Sort.Direction.DESC, "createdAt"))
        return mongoTemplate.find(query, StudyGroupMember::class.java)
    }

    override fun findByGroupIdIncludingDeleted(
        groupId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMember> {
        val criteria = Criteria.where("groupId").`is`(groupId)
        if (cursor != null) {
            criteria.and("_id").gt(cursor)
        }
        return mongoTemplate.find(
            Query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "_id"))
                .limit(limit),
            StudyGroupMember::class.java
        )
    }

    override fun findActiveByGroupId(
        groupId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<StudyGroupMember> {
        val criteria = Criteria.where("groupId").`is`(groupId).and("isDeleted").ne(true)
        if (cursor != null) {
            criteria.and("_id").gt(cursor)
        }
        return mongoTemplate.find(
            Query(criteria)
                .with(Sort.by(Sort.Direction.ASC, "_id"))
                .limit(limit),
            StudyGroupMember::class.java
        )
    }

    override fun countByGroupId(groupId: ObjectId): Long {
        val query = Query(Criteria.where("groupId").`is`(groupId).and("isDeleted").ne(true))
        return mongoTemplate.count(query, StudyGroupMember::class.java)
    }

    override fun findByGroupIdsOrderByCreatedAt(
        groupIds: List<ObjectId>,
        limit: Int
    ): Map<String, List<StudyGroupMember>> {
        val query = Query(Criteria.where("groupId").`in`(groupIds).and("isDeleted").ne(true))
            .with(Sort.by(Sort.Direction.DESC, "createdAt"))
            .limit(limit)

        val members = mongoTemplate.find(query, StudyGroupMember::class.java)

        return members.groupBy { it.groupId.toHexString() }
            .mapValues { it.value.sortedByDescending { member -> member.createdAt } }
    }

    override fun findGroupIdsByMemberId(memberId: ObjectId): List<String> {
        val query = Query(Criteria.where("memberId").`is`(memberId).and("isDeleted").ne(true))
        return mongoTemplate.find(query, StudyGroupMember::class.java)
            .map { it.groupId.toHexString() }
            .distinct()
    }

    override fun findByGroupIdAndMemberIdsIn(
        groupId: ObjectId,
        memberIds: List<ObjectId>
    ): List<StudyGroupMember> {
        return mongoTemplate.find(
            Query(Criteria.where("groupId").`is`(groupId)
                .and("memberId").`in`(memberIds)
                .and("isDeleted").ne(true)),
            StudyGroupMember::class.java
        )
    }

    override fun countByMemberIdAndRole(memberId: ObjectId, role: GroupMemberRole): Long {
        return mongoTemplate.count(
            Query(Criteria.where("memberId").`is`(memberId)
                .and("role").`is`(role)
                .and("isDeleted").ne(true)),
            StudyGroupMember::class.java
        )
    }

    override fun findManagedGroupIds(
        memberId: ObjectId,
        cursor: ObjectId?,
        limit: Int
    ): List<ObjectId> {
        val criteria = Criteria.where("memberId").`is`(memberId)
            .and("role").`in`(GroupMemberRole.OWNER, GroupMemberRole.ADMIN)
            .and("isDeleted").ne(true)
        if (cursor != null) {
            criteria.and("groupId").gt(cursor)
        }
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(criteria),
            Aggregation.group("groupId"),
            Aggregation.lookup("study_groups", "_id", "_id", "group"),
            Aggregation.unwind("group"),
            Aggregation.match(Criteria.where("group.isDeleted").ne(true)),
            Aggregation.sort(Sort.Direction.ASC, "_id"),
            Aggregation.limit(limit.toLong())
        )
        return mongoTemplate.aggregate(
            aggregation,
            "study_group_members",
            Document::class.java
        ).mappedResults.mapNotNull { it["_id"] as? ObjectId }
    }

    override fun countDistinctActiveMembers(groupIds: List<ObjectId>): Map<ObjectId, Long> {
        if (groupIds.isEmpty()) {
            return emptyMap()
        }
        val aggregation = Aggregation.newAggregation(
            Aggregation.match(
                Criteria.where("groupId").`in`(groupIds)
                    .and("isDeleted").ne(true)
            ),
            Aggregation.group("groupId", "memberId"),
            Aggregation.group("_id.groupId").count().`as`("count")
        )
        return mongoTemplate.aggregate(
            aggregation,
            "study_group_members",
            Document::class.java
        ).mappedResults.mapNotNull { document ->
            val groupId = document["_id"] as? ObjectId ?: return@mapNotNull null
            val count = (document["count"] as? Number)?.toLong() ?: 0L
            groupId to count
        }.toMap()
    }
}
