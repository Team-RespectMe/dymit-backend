package net.noti_me.dymit.dymit_backend_api.member.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.member.domain.DailyMemberStatus
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.DailyMemberStatusRepository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
class MongoDailyMemberStatusRepository(
    private val mongoTemplate: MongoTemplate
): DailyMemberStatusRepository {

    override fun save(status: DailyMemberStatus): DailyMemberStatus? {
        return mongoTemplate.save(status)
    }

    override fun findAllByCreatedAtBetween(start: LocalDateTime, end: LocalDateTime): List<DailyMemberStatus> {
        val query = Query()
            .addCriteria(
                Criteria.where("createdAt")
                    .gte(start)
                    .lt(end)
            )
        return mongoTemplate.find(query, DailyMemberStatus::class.java)
    }
}