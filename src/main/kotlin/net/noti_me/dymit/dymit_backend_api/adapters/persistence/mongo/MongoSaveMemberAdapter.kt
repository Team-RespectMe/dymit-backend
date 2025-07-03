package net.noti_me.dymit.dymit_backend_api.adapters.persistence.mongo

import net.noti_me.dymit.dymit_backend_api.ports.persistence.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.domain.member.Member
import org.springframework.stereotype.Repository
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.update

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
