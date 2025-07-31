package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo.member

import net.noti_me.dymit.dymit_backend_api.ports.persistence.member.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.MongoTemplate

@Repository
class MongoSaveMemberAdapter(
    private val mongoTemplate: MongoTemplate
) : SaveMemberPort {

    override fun persist(member: Member): Member {
        return mongoTemplate.save(member)
    }

    override fun update(member: Member): Member {
        return mongoTemplate.save(member)
    }

    override fun delete(member: Member): Boolean {
        return try {
            mongoTemplate.remove(member)
            true
        } catch (e: Exception) {
            false
        }
    }
}
