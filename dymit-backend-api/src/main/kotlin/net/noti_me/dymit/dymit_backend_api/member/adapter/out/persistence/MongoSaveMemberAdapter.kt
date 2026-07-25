package net.noti_me.dymit.dymit_backend_api.member.adapter.out.persistence

import net.noti_me.dymit.dymit_backend_api.member.application.port.out.persistence.SaveMemberPort
import net.noti_me.dymit.dymit_backend_api.member.domain.Member
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
        member.oidcIdentities.clear();
        member.markAsDeleted()
        mongoTemplate.save(member)
        return true
    }
}
