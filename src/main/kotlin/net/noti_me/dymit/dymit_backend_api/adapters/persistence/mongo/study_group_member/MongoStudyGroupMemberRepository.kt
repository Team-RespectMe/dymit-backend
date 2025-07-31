package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.study_group_member

import net.noti_me.dymit.dymit_backend_api.domain.studyGroup.StudyGroupMember
import net.noti_me.dymit.dymit_backend_api.ports.persistence.study_group_member.StudyGroupMemberRepository
import org.bson.Document
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.aggregation.Aggregation
import org.springframework.data.mongodb.core.aggregation.AggregationOperation
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import kotlin.collections.aggregate

@Repository
class MongoStudyGroupMemberRepository(
    private val mongoTemplate: MongoTemplate
): StudyGroupMemberRepository {

    override fun findByGroupIdAndMemberId(groupId: String, memberId: String): StudyGroupMember? {
        return mongoTemplate.findOne(
            Query(Criteria("groupId").`is`(groupId)
                .and("memberId").`is`(memberId)
            ),
            StudyGroupMember::class.java
        )
    }

    override fun persist(member: StudyGroupMember): StudyGroupMember {
        return mongoTemplate.save(member)
    }

    override fun update(member: StudyGroupMember): StudyGroupMember {
        return mongoTemplate.save(member)
    }

    override fun delete(member: StudyGroupMember): Boolean {
        return mongoTemplate.remove(member).deletedCount > 0
    }

    override fun countByGroupId(groupId: String): Long {
        return mongoTemplate.count(
            Query(Criteria.where("groupId").`is`(groupId)),
            StudyGroupMember::class.java
        )
    }

    override fun findByGroupIdsOrderByCreatedAt(
        groupIds: List<String>,
        limit: Int
    ): Map<String, List<StudyGroupMember>> {
        val matchStage = Aggregation.match(Criteria.where("groupId").`in`(groupIds))
        val setWindowFieldsStage = AggregationOperation { context ->
            Document(
                "\$setWindowFields", Document()
                    .append("partitionBy", "\$groupId")
                    .append("sortBy", Document("createdAt", 1))
                    .append("output", Document("rank", Document("\$documentNumber", Document())))
            )
        }

        val matchRankStage = Aggregation.match(Criteria.where("rank").lte(limit))

        val aggregation = Aggregation.newAggregation(
            matchStage,
            setWindowFieldsStage,
            matchRankStage
        )
        val results = mongoTemplate.aggregate(aggregation, "study_group_members", StudyGroupMember::class.java)
            .mappedResults

        return results.groupBy { it.groupId }
    }

    override fun findGroupIdsByMemberId(memberId: String): List<String> {
        return mongoTemplate.find(
            Query(Criteria.where("memberId").`is`(memberId)),
            StudyGroupMember::class.java
        ).map { it.groupId }.distinct()
    }
}