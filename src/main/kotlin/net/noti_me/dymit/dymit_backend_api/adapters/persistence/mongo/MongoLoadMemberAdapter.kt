package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo

import org.springframework.stereotype.Repository
import net.noti_me.dymit.dymit_backend_api.ports.persistence.LoadMemberPort
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import net.noti_me.dymit.dymit_backend_api.domain.member.OidcIdentity
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

@Repository
class MongoLoadMemberAdapter(
    private val mongoTemplate: MongoTemplate
) : LoadMemberPort {

    override fun loadById(id: String): Member? {
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
}