package net.noti_me.dymit.dymit_backend_api.member.adapter.out.persistence

import org.springframework.stereotype.Repository
import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
import net.noti_me.dymit.dymit_backend_api.member.domain.OidcIdentity
import org.bson.types.ObjectId
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import java.time.Instant

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
        start: Instant,
        end: Instant
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
        start: Instant,
        end: Instant,
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
