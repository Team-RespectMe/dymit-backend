package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.member

import org.springframework.stereotype.Repository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.LocalDateTime

@Repository
class MongoLoadMemberAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadMemberPort {

    override fun loadById(id: String): Member? {
        return mongoTemplate.findById(id, Member::class.java)
    }

    override fun loadById(id: ObjectId): Member? {
        return mongoTemplate.findById(id, Member::class.java)
    }

    override fun loadByOidcIdentity(
        oidcIdentity: OidcIdentity
    ): Member? {
        return mongoTemplate.findOne(
            Query(Criteria.where("oidcIdentities").elemMatch(
                Criteria.where("provider").`is`(oidcIdentity.provider)
                    .and("subject").`is`(oidcIdentity.subject)
            )),
            Member::class.java
        )
    }

    override fun existsByNickname(nickname: String): Boolean {
        return mongoTemplate.exists(
            Query(Criteria.where("nickname").`is`(nickname)),
            Member::class.java
        )
    }

    override fun loadByIds(ids: List<String>): List<Member> {
        return mongoTemplate.find(
            Query(Criteria.where("_id").`in`(ids)),
            Member::class.java
        )
    }

    override fun loadByDeviceToken(deviceToken: String): List<Member> {
        return mongoTemplate.find(
            Query(Criteria.where("deviceTokens.token").`is`(deviceToken)),
            Member::class.java
        )
    }

    override fun countByCreatedAtBetween(
        start: LocalDateTime,
        end: LocalDateTime
    ): Long {
        return mongoTemplate.count(
            Query(
                Criteria.where("createdAt")
                    .gte(start)
                    .lt(end)
            ),
            Member::class.java
        )
    }

    override fun countByLastAccessedAtBetween(
        start: LocalDateTime,
        end: LocalDateTime,
        isDeleted: Boolean
    ): Long {
        return mongoTemplate.count(
            Query(
                Criteria.where("lastAccessAt")
                    .gte(start)
                    .lt(end)
                    .and("isDeleted").`is`(isDeleted)
            ),
            Member::class.java
        )
    }

    override fun countAll(): Long {
        return mongoTemplate.count(
            Query(),
            Member::class.java
        )
    }
}
