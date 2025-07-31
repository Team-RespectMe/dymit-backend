package net.noti_me.dymit.dymit_backend_api.ports.persistence.member

import net.noti_me.dymit.dymit_backend_api.domain.member.Member

interface SaveMemberPort {

    fun persist(member: Member): Member

    fun update(member: Member): Member

    fun delete(member: Member): Boolean
}
